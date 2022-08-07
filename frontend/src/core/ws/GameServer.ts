import 'stompjs'

class GameServer {
  private client: Client | undefined

  constructor() {
  }

  async connect(playerName: string) {
    return new Promise<void>((resolve, reject) => {
      this.client = Stomp.over(new WebSocket(`ws://${window.location.host}/api?playerToken=${encodeURIComponent(btoa(playerName))}`, ['v10.stomp', 'v11.stomp']))
      this.client.connect(
        {},
        () => {
          resolve()
        },
        (e) => {
          reject(e)
        })
    })
  }
}

export const server = new GameServer()
