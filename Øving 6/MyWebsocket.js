class MyWebsocket {

	const ws;

	constructor(portNr) {
		this.ws = new WebSocket('ws://localhost:3210', ['json', 'xml']);
	}

	sendMessage(message) {
		const json = JSON.stringify({message: message});
		ws.send(json);
	}
}