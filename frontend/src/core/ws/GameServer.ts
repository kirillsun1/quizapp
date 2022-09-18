import 'stompjs'
import { Room } from '../../domain/Room'

type GameServerResponseBase = {
  error: true;
  errorCode: string;
} | {
  error: false
};

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
    const response = await this.convertSendAndReceive({operationId: 'rooms.create'})
    if (!this.isRoomResponse(response)) {
      throw Error(`Bad response from server ${JSON.stringify(response)}`)
    }
    return response.room
  }

  async joinRoom(roomCode: string): Promise<Room> {
    const response = await this.convertSendAndReceive({operationId: `rooms/${roomCode}.join`})
    if (!this.isRoomResponse(response)) {
      throw Error(`Bad response from server ${JSON.stringify(response)}`)
    }
    return response.room
  }

  private isRoomResponse(response: unknown): response is { room: Room } {
    return typeof response === 'object'
      && response !== null
      && 'room' in response
      && (response as any).room !== null
  }

  async assignQuiz(roomCode: string, quizId: number) {
    await this.convertSendAndReceive({
      operationId: `rooms/${roomCode}/quiz.assign`,
      body: {quizId},
    })
  }

  async moveOn(roomCode: string) {
    await this.convertSendAndReceive({
      operationId: `rooms/${roomCode}/quiz.move-on`,
    })
  }

  async vote(roomCode: string, answer: number) {
    await this.convertSendAndReceive({
      operationId: `rooms/${roomCode}/quiz.vote`,
      body: {choice: answer},
    })
  }

  subscribeToRoomChanges(roomCode: string, callback: (room: Room) => void) {
    if (!this.client) {
      throw new Error('No client')
    }
    this.client.subscribe(`/topic/rooms.${roomCode}`, message => {
      console.debug(`Received room[${roomCode}] changed event`, message)
      callback(JSON.parse(message.body).room)
    })
  }

  private convertSendAndReceive({
                                  operationId,
                                  body,
                                }: { operationId: string, body?: any }): Promise<GameServerResponseBase> {
    return new Promise<GameServerResponseBase>((resolve, reject) => {
      if (!this.client) {
        throw new Error('No client')
      }
      const subscriptionKey = `${new Date()}-${operationId}`
      const subscription = this.client.subscribe(`/user/queue/${operationId}`, message => {
        try {
          const body = JSON.parse(message.body)
          if (!this.isExpectedResponse(body)) {
            reject(`Unknown response from server ${message.body}`)
          } else if (body.error) {
            reject(body.errorCode)
            console.error(`Operation ${operationId} failed with code ${body.errorCode}.`)
          } else {
            resolve(body)
          }
        } catch (e) {
          reject(e)
        } finally {
          this.stompSubscriptions.get(subscriptionKey)?.unsubscribe()
          this.stompSubscriptions.delete(subscriptionKey)
        }
      })
      this.stompSubscriptions.set(subscriptionKey, subscription)
      this.client.send(`/app/${operationId}`, {}, body ? JSON.stringify(body) : '')
    })
  }

  private isExpectedResponse(body: unknown): body is GameServerResponseBase {
    return typeof body === 'object' && body !== null && 'error' in body
  }
}

export const server = new GameServer()
