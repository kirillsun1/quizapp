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

  async createRoom(): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      if (!this.client) {
        throw new Error('No client')
      }
      let subscription: Subscription
      subscription = this.client.subscribe('/user/queue/rooms.create', message => {
        try {
          const code = JSON.parse(message.body).code
          console.log('Created room', code)
          resolve(code)
          this.client?.unsubscribe(subscription.id)
        } catch (e) {
          reject(e)
        }
      })
      this.client.send('/app/rooms.create', {}, '')
    })

  }
}

export const server = new GameServer()
