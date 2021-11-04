//const MyWebSocket = require('./MyWebsocket.js');

//let object = new MyWebSocket(3210);

const ws = new WebSocket('ws://localhost:3210', ['json', 'xml']);

    	let input = document.getElementById("code");
    	let output = document.getElementById("output");
    	let btn = document.getElementById("btn");
    	let info = document.getElementById("info");

    	ws.addEventListener('open', () => {
  			console.log("hei");
		});	

		ws.addEventListener('message', event => {
  			const data = JSON.parse(event.data);
  			console.log(data);

  			output.value = data.message;
		});

		btn.onclick = function() {
			if (input.value !== "") {
				const json = JSON.stringify({message: input.value});
				ws.send(json);
				//object.sendMessage(input.value);
				info.innerHTML = "Message sent."
				input.value = "";
			} else {
				info.innerHTML = "Input must contain message.";
			}
		};