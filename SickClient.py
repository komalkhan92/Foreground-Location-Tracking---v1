import socket
import time
import sys

IP = "127.0.0.1"
PORT = 1234
FILL_TO = 4
SLEEP = 7

class Client(object):
    def __init__(self):
        """
        constructor
        """
        self.client_socket = None
        self.lat = input("Enter latitude")
        self.long = input("Enter longitude")
        try:
            self.initiate_client_socket(IP, PORT)
        except Exception as msg:
            print (msg)
            sys.exit(-1)

    def initiate_client_socket(self, ip, port):
        """
        initiates client socket and returns it
        """
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client_socket.connect((ip, port))


    def send_location_to_server(self):
        """
        Sends the location to the server according to the protocol
        """
        location = str(self.lat) + "," + str(self.long)
        message_len = str(len(location.encode())).zfill(FILL_TO)
        location = message_len + location
        self.client_socket.send(location.encode())


    def handle_movement(self):
        """
        send location to server every 7 seconds
        """
        while True:
            self.send_location_to_server()
            print("Sent location: " + str(self.lat) + " , " + str(self.long))
            time.sleep(SLEEP)

    def declare(self):
        """
        declare sickness
        """
        msg = "SICK"
        message_len = str(len(msg.encode())).zfill(FILL_TO)
        msg = message_len + msg
        self.client_socket.send(msg.encode())


def main():
    """
    set location, initiate socket, declare sickness
    and start sending the location to the server
    """
    try:
        client = Client()
        client.declare()
        client.handle_movement()
    except socket.error as msg:
        print("socket error: ", msg, "main()")
    except Exception as msg:
        print ("general error: ", msg, "main()")

if __name__ == '__main__':
    main()
