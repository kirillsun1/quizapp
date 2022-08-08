import 'stompjs'
import { Room } from '../../domain/Room'

class GameServer {
  private client: Client | undefined
  private stompSubscriptions: Map<string, Subscription> = new Map()

  constructor() {
  }

  async connect(playerName: string) {
    return new Promise<void>((resolve, reject) => {
      try {
        this.client = Stomp.over(new WebSocket(`ws://${window.location.host}/api?playerToken=${encodeURIComponent(btoa(playerName))}`, ['v10.stomp', 'v11.stomp']))
        this.client.connect(
          {},
          () => {
            resolve()
          },
          (e) => {
            reject(e)
          })
      } catch (e) {
        console.error('Could not connect to server', e)
        reject(e)
      }
    })
  }

  async createRoom(): Promise<Room> {
    return new Promise<Room>((resolve, reject) => {
      if (!this.client) {
        throw new Error('No client')
      }
      const key = `rooms.create-${new Date()}`
      const subscription = this.client.subscribe('/user/queue/rooms.create', message => {
        try {
          const room = JSON.parse(message.body).room
          if (room) {
            resolve(room)
          } else {
            reject('Didn\'t receive a proper room -> ' + room)
          }
        } catch (e) {
          reject(e)
        } finally {
          this.stompSubscriptions.get(key)?.unsubscribe()
          this.stompSubscriptions.delete(key)
        }
      })
      this.stompSubscriptions.set(key, subscription)
      this.client.send('/app/rooms.create', {}, '')
    })
  }

  async joinRoom(roomCode: string): Promise<Room> {
    return new Promise<Room>((resolve, reject) => {
      if (!this.client) {
        throw new Error('No client')
      }
      const key = `rooms${roomCode}.join-${new Date()}`
      const subscription = this.client.subscribe(`/user/queue/rooms/${roomCode}.join`, message => {
        try {
          const body = JSON.parse(message.body)
          const ok = body.ok
          if (ok && body.room)
            resolve(body.room)
          else
            reject()
        } catch (e) {
          reject(e)
        } finally {
          this.stompSubscriptions.get(key)?.unsubscribe()
          this.stompSubscriptions.delete(key)
        }
      })
      this.stompSubscriptions.set(key, subscription)
      this.client.send(`/app/rooms/${roomCode}.join`, {}, '')
    })
  }

  subscribeToRoomChanges(roomCode: string, callback: (room: Room) => void) {
    if (!this.client) {
      throw new Error('No client')
    }
    this.client.subscribe(`/topic/rooms.${roomCode}`, message => {
      console.debug(`Received room[${roomCode}] changed event`, message)
      callback(JSON.parse(message.body))
    })
  }
}

export const server = new GameServer()
