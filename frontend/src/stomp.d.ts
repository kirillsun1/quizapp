// types for stompjs library. The library itself doesn't provide them for web browser.

declare var Stomp: {
  client(url: string, protocols?: string | Array<string>): Client;
  over(ws: WebSocket): Client;
  VERSIONS: {
    V1_0: string,
    V1_1: string,
    V1_2: string,
    supportedVersions: () => string[]
  };
}

declare class Client {
  connected: boolean
  counter: number
  heartbeat: {
    incoming: number,
    outgoing: number
  }
  maxWebSocketFrameSize: number
  subscriptions: {}
  ws: WebSocket

  debug(...args: string[]): any;

  connect(headers: { login: string, passcode: string, host?: string | undefined }, connectCallback: (frame?: Frame) => any, errorCallback?: (error: Frame | string) => any): any;
  connect(headers: {}, connectCallback: (frame?: Frame) => any, errorCallback?: (error: Frame | string) => any): any;
  connect(login: string, passcode: string, connectCallback: (frame?: Frame) => any, errorCallback?: (error: Frame | string) => any, host?: string): any;

  disconnect(disconnectCallback: () => any, headers?: {}): any;

  send(destination: string, headers?: {}, body?: string): any;

  subscribe(destination: string, callback?: (message: Message) => any, headers?: {}): Subscription;

  unsubscribe(id: string): void;

  begin(transaction: string): any;

  commit(transaction: string): any;

  abort(transaction: string): any;

  ack(messageID: string, subscription: string, headers?: {}): any;

  nack(messageID: string, subscription: string, headers?: {}): any;
}

declare interface Subscription {
  id: string;

  unsubscribe(): void;
}

declare interface Message extends Frame {
  ack(headers?: {}): any;

  nack(headers?: {}): any;
}

declare class Frame {
  command: string
  headers: {}
  body: string

  constructor(command: string, headers?: {}, body?: string);

  toString(): string;

  static sizeOfUTF8(s: string): number;

  static unmarshall(datas: any): any;

  static marshall(command: string, headers?: {}, body?: string): any;
}


