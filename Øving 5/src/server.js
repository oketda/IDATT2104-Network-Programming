let http = require("http");
let fs = require("fs");
let bodyParser = require('body-parser');
let express = require('express');
let path = require('path');
let { exec } = require('child_process');

let app = express();
let host = "localost";
let port = 8000;
let url = "/main.html";

app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());
app.use(express.static(path.join(__dirname)));

app.get("/", (req, res) => {
	res.sendFile(path.join(__dirname, 'main.html'));
});

app.post("/code", async(req, res) => {
	writeToFile(req.body.code);
	let result = await buildDockerfile();

	exec("docker run cpp-image", (err, stdout, stderr) => {
		if (err) {
			 res.send(JSON.stringify({
				compiled: stderr
			}))
		}
		res.send(JSON.stringify({
			compiled: stdout
		}));
		console.log("Returned answer");
	});
});


let server = app.listen(port, () => {
		console.log("Server listening to port: " + port);
});



function writeToFile(code){
	fs.writeFile("main.cpp", code, (er) => {
		if (er) throw er;
		console.log("File written");

	});
};

async function buildDockerfile(){
	return new Promise(resolve => {
		exec("docker build -t cpp-image .", () => {
			console.log("Docker image built");
			resolve(1);
		});
	})
}
