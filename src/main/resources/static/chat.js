"use strict";

async function sendRequest(url, type, data=null) {
    const request = {
        method: type,
        mode: 'cors',
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json'
        },
        redirect: 'follow',
        referrerPolicy: 'no-referrer',
    }
    if (data != null) request.body = JSON.stringify(data);
    return await fetch(url, request);
}

function send() {
    const input = document.getElementById('messageInput').value;
    const data = { key: input, value: ['1', '2', '3']};
    return sendRequest('/push',"POST", data);
}

function subscribe() {
    window.eventSource = registerSSE('/subscribe')
}


async function pull() {
    const response = await sendRequest('/pull', "GET");
    const data = await response.json(); // or response.json() if you expect JSON
    const li = document.createElement("li");
    li.appendChild(document.createTextNode(data.key));
    const ul = document.getElementById("list");
    ul.appendChild(li);
}
function handleChatEvent(eventData) {
    const li = document.createElement("li");
    li.appendChild(document.createTextNode(eventData.key));
    const ul = document.getElementById("list");
    ul.appendChild(li);
}

function registerSSE(url) {
    const source = new EventSource(url);
    source.addEventListener('broadcastedMessage', event => {
        console.log(event);
        handleChatEvent(JSON.parse(event.data));
    })
    source.onopen = event => console.log("Connection opened");
    source.onerror = event => console.error("Connection error");
    return source;
}

window.assignedName = "Unknown";
window.assignedColor = "000000";



