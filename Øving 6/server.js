
const http = require('http');
const crypto = require('crypto');
const static = require('node-static');
const fs = require("fs").promises;
const file = new static.Server('./');

let host = "localhost";
let port = 3210;
let indexFile;
let sockets = [];

const server = http.createServer((req, res) => {
  res.setHeader("Content-Type", "text/html");
    res.writeHead(200);
    res.end(indexFile);
});

fs.readFile(`${__dirname}/index.html`)
  .then((contents) => {
    indexFile = contents;
    server.listen(port, host, () =>
      console.log(`Server is running on http://${host}:${port}`)
    );
  })
  .catch((err) => {
    console.error(`Could not read index.html file: ${err}`);
    process.exit(1);
  });


server.on('upgrade', function (req, socket) {
  if (req.headers['upgrade'] !== 'websocket') {
    socket.end('HTTP/1.1 400 Bad Request');
    return;
  }
  const acceptKey = req.headers['sec-websocket-key'];  
  const hash = generateAcceptValue(acceptKey); 
  const responseHeaders = [ 'HTTP/1.1 101 Web Socket Protocol Handshake', 'Upgrade: WebSocket', 'Connection: Upgrade', `Sec-WebSocket-Accept: ${hash}` ]; 
  
  const protocol = req.headers['sec-websocket-protocol'];
  const protocols = !protocol ? [] : protocol.split(',').map(s => s.trim());

  if (protocols.includes('json')) {
    responseHeaders.push(`Sec-WebSocket-Protocol: json`);
  }

  socket.write(responseHeaders.join('\r\n') + '\r\n\r\n');

  sockets.push(socket);

  socket.on('data', buffer => {
  const message = parseMessage(buffer);
  if (message) {
  console.log(message);
  //const actualMessage = JSON.parse(message);
  //console.log(actualMessage);

  try {
  	sockets.forEach((s) =>
  		s.write(constructReply({ message: 'Server acknowledges recieved message: ' + message.message })));
  } catch (e) {
  	console.log("error: ", e)
  }
  } else if (message === null) { 
      console.log('WebSocket connection closed by the client.'); 
  }
});
});

function constructReply (data) {
  const json = JSON.stringify(data)
  const jsonByteLength = Buffer.byteLength(json);
  const lengthByteCount = jsonByteLength < 126 ? 0 : 2; 
  const payloadLength = lengthByteCount === 0 ? jsonByteLength : 126; 
  const buffer = Buffer.alloc(2 + lengthByteCount + jsonByteLength); 

  buffer.writeUInt8(0b10000001, 0); 
  buffer.writeUInt8(payloadLength, 1); 
  let payloadOffset = 2; 
  if (lengthByteCount > 0) { 
    buffer.writeUInt16BE(jsonByteLength, 2); payloadOffset += lengthByteCount; 
  } 
  buffer.write(json, payloadOffset); 
  return buffer;
}

function parseMessage (buffer) {
  const firstByte = buffer.readUInt8(0);
  const isFinalFrame = Boolean((firstByte >>> 7) & 0x1); 
  const [reserved1, reserved2, reserved3] = [ Boolean((firstByte >>> 6) & 0x1), Boolean((firstByte >>> 5) & 0x1), Boolean((firstByte >>> 4) & 0x1) ]; 
  const opCode = firstByte & 0xF; 
 
  if (opCode === 0x8) 
     return null; 

  if (opCode !== 0x1) 
    return; 

  const secondByte = buffer.readUInt8(1); 
  const isMasked = Boolean((secondByte >>> 7) & 0x1); 
  let currentOffset = 2; let payloadLength = secondByte & 0x7F; 
  if (payloadLength > 125) { 
    if (payloadLength === 126) { 
      payloadLength = buffer.readUInt16BE(currentOffset); 
      currentOffset += 2; 
    } else { 
      const leftPart = buffer.readUInt32BE(currentOffset); 
      const rightPart = buffer.readUInt32BE(currentOffset += 4); 
      throw new Error('Large payloads not currently implemented'); 
    } 
  }

 let maskingKey;
 if (isMasked) {
  	maskingKey = buffer.readUInt32BE(currentOffset);
  	currentOffset += 4;
   }
   const data = Buffer.alloc(payloadLength);
if (isMasked) {
  for (let i = 0, j = 0; i < payloadLength; ++i, j = i % 4) {
    const shift = j == 3 ? 0 : (3 - j) << 3; 
    const mask = (shift == 0 ? maskingKey : (maskingKey >>> shift)) & 0xFF;
    const source = buffer.readUInt8(currentOffset++); 
    data.writeUInt8(mask ^ source, i);
  }
 } else {
  buffer.copy(data, 0, currentOffset++);
}
	const json = data.toString('utf8');
	return JSON.parse(json);
}


function generateAcceptValue (acceptKey) {
  return crypto
  .createHash('sha1')
  .update(acceptKey + '258EAFA5-E914-47DA-95CA-C5AB0DC85B11', 'binary')
  .digest('base64');
}