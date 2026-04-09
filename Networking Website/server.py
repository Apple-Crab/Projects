# CSE 4344 - Lab 1 Web Server
# NAME: Andrew Whitmill
# ID: 1001839036
# References Used:
#   https://www.w3schools.com/html/html_basic.asp
#   https://datatracker.ietf.org/doc/html/rfc7231
#   https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status

import socket
import threading

HOST = "localhost"
PORT = 8080

# Function: get_content_type
# Determines content type based on file extension
def get_content_type(filename):
    if filename.endswith(".html"):
        return "text/html"
    elif filename.endswith(".jpg") or filename.endswith(".jpeg"):
        return "image/jpeg"
    elif filename.endswith(".png"):
        return "image/png"
    elif filename.endswith(".gif"):
        return "image/gif"
    else:
        return "application/octet-stream"

# Function: handle_client
# Handles a single HTTP client request in its own thread
def handle_client(client_socket):
    try:

        # Receive HTTP request from browser
        request = client_socket.recv(1024).decode()

        print("---HTTP Request---")
        print(request)

        # Parse the request line
        request_line = request.split("\n")[0]
        method, path, version = request_line.split()

        # Remove leading /
        filename = path[1:]

        if filename == "":
            filename = "index.html"

        # 301 REDIRECT IMPLEMENTATION
        # page1.html automatically changes to page2.html
        if filename == "page1.html":

            header = "HTTP/1.0 301 Moved Permanently\r\n"
            header += "Location: /page2.html\r\n"
            header += "\r\n"

            client_socket.send(header.encode())
            client_socket.close()
            return

        # Attempt to open requested file
        try:

            with open(filename, "rb") as f:
                content = f.read()

            content_type = get_content_type(filename)

            # Build HTTP response header
            header = "HTTP/1.0 200 OK\r\n"
            header += f"Content-Type: {content_type}\r\n"
            header += f"Content-Length: {len(content)}\r\n"

            # Set custom cookie header
            header += "Set-Cookie: mycookie=hello; Path=/\r\n"

            header += "\r\n"

            # Send header and file content
            client_socket.send(header.encode())
            client_socket.send(content)

        # 404 ERROR HANDLING
        except FileNotFoundError:

            with open("404.html", "rb") as f:
                content = f.read()

            header = "HTTP/1.0 404 Not Found\r\n"
            header += "Content-Type: text/html\r\n"
            header += f"Content-Length: {len(content)}\r\n"
            header += "\r\n"

            client_socket.send(header.encode())
            client_socket.send(content)

    except Exception as e:
        print("Error:", e)

    finally:
        client_socket.close()

# Main server setup
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

server_socket.bind((HOST, PORT))
server_socket.listen(5)

print(f"Web Server running at http://{HOST}:{PORT}")

while True:

    client_socket, addr = server_socket.accept()

    print("Connection from", addr)

    thread = threading.Thread(target=handle_client, args=(client_socket,))
    thread.start()
