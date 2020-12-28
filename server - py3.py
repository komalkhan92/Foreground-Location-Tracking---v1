import socket
import threading
import sys
import math
from decimal import Decimal


MSG_LEN = 4
IP = '0.0.0.0'
PORT = 1234
TOMETERS = 100000
MINRAD = 5
FILL_TO = 4
SQUARED = 2
LAT = 0
LONG = 1


class Server(object):
    def __init__(self):
        """ constructor"""
        self.server_socket = None
        self.my_clients = {}
        self.sick_locations = {}
        try:
            # initiating server socket
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

            # the server binds itself to a certain socket
            self.server_socket.bind((IP, PORT))

            # listening to the socket
            self.server_socket.listen(10)
        except socket.error as e:
            print("booz!!", e)
            sys.exit(-1)
        except Exception as e:
            print("booz!!!", e)
            sys.exit(-1)

    def handle_single_client(self, client_socket, address):
        """ thread function which handles a single client  in a loop """
        data = None
        while data != '' and data != 'QUIT':
            try:
                # receiving data
                raw_data = client_socket.recv(MSG_LEN)
                data = raw_data.decode()
                if data.isdigit():
                    raw_data = client_socket.recv(int(data))
                    print(raw_data.decode())
                    msg = self.handle_response(client_socket, address, raw_data.decode())
                else:
                    print("received illegal size: ", raw_data)
                    msg = "received illegal size"
                message_len = str(len(msg.encode())).zfill(FILL_TO)
                msg = message_len + msg
                client_socket.send(msg.encode())
            except socket.error as msg:
                print("socket failure: ", msg)
                data = ''
            except Exception as msg:
                print("exception: ", msg)
                data = ''

    @staticmethod
    def calculateDistance(latF, longF, latS, longS):
        """
        gets the latitude and longitude of two locations
        and returns the distance between them in meters
        """
        return math.sqrt(math.pow(latF-latS, SQUARED) + math.pow(longF-longS, SQUARED))*TOMETERS

    def handle_response(self, client_socket, address, response):
        """
        if the client declares he is sick change his status in the dictionary
        if the client is known as sick update his location
        """
        if response == "SICK":
            self.my_clients[client_socket, address] = "SICK"
            return "Thanks for updating, get well soon"
        elif self.my_clients[client_socket, address] == "SICK":
            response_arr = response.split(',')
            lat = Decimal(response_arr[LAT])
            long = Decimal(response_arr[LONG])
            self.sick_locations[client_socket, address] = lat, long
            return "Please go home"
        else:
            response_arr = response.split(',')
            lat = Decimal(response_arr[LAT])
            long = Decimal(response_arr[LONG])
            return self.check_surrounding(lat, long)

    def check_surrounding(self, lat, long):
        for k in self.sick_locations:
            latSick, longSick = self.sick_locations[k]
            if self.calculateDistance(lat, long, latSick, longSick) < MINRAD:
                return "CODE RED"
        return "ALL WELL"

    def handle_clients(self):
            """
            handle a single client
            accepts a connection request and call handle _client
            for receiving its requests
            """
            done = False
            while not done:
                try:
                    # accepting a connect request
                    client_socket, address = self.server_socket.accept()
                    print("client accepted")
                    # add client to dictionary
                    self.my_clients[client_socket, address] = "accepted"
                    clnt_thread = threading.Thread(target=self.handle_single_client,
                                                   args=(client_socket, address, ))
                    clnt_thread.start()

                except socket.error as msg:
                    print("socket failure: ", msg)
                    done = True

                except Exception as msg:
                    print("exception: ", msg)
                    done = True


def main():
    """
    server main - receives a location and alerts the client if needed
    """
    try:
        srvr = Server()
        srvr.handle_clients()
    except socket.error as msg:
        print("socket failure: ", msg)
    except Exception as msg:
        print("exception: ", msg)

if __name__ == '__main__':
    main()
