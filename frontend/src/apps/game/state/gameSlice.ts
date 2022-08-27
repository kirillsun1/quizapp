import { Action, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { server } from '../../../core/ws/GameServer'
import { Room } from '../../../domain/Room'

type Loadings = {
  createRoom: boolean
  joinRoom: boolean
}

type GameState = {
  playerName: string
  room?: Room
  loadings: Loadings
}

const createRoom = createAsyncThunk<void, undefined>(
  'CREATE_ROOM',
  async (__, {dispatch}) => {
    dispatch(slice.actions.changeLoading({loadingKey: 'createRoom', value: true}))
    const room = await server.createRoom()
    dispatch(slice.actions.changeLoading({loadingKey: 'createRoom', value: false}))
    dispatch(slice.actions.updateRoom(room))
    server.subscribeToRoomChanges(room.code, (room) => dispatch(slice.actions.updateRoom(room)))
  })

const joinRoom = createAsyncThunk<void, string>(
  'JOIN_ROOM',
  async (roomCode, {dispatch}) => {
    dispatch(slice.actions.changeLoading({loadingKey: 'joinRoom', value: true}))
    const room = await server.joinRoom(roomCode)
    dispatch(slice.actions.changeLoading({loadingKey: 'joinRoom', value: false}))
    dispatch(slice.actions.updateRoom(room))
    server.subscribeToRoomChanges(roomCode, (room) => dispatch(slice.actions.updateRoom(room)))
  })

const assignQuiz = createAsyncThunk<void, number, { state: { game: GameState } }>(
  'ASSIGN_QUIZ',
  async (quizId, api) => {
    const state = api.getState()
    if (state.game.room?.code) {
      await server.assignQuiz(state.game.room.code, quizId)
    }
  },
)

const moveOn = createAsyncThunk<void, never, { state: { game: GameState } }>(
  'MOVE_ON',
  async (_, {getState}) => {
    await server.moveOn(getState().game.room!.code)
  })


const vote = createAsyncThunk<void, number, { state: { game: GameState } }>(
  'VOTE',
  async (answer, {getState}) => {
    await server.vote(getState().game.room!.code, answer)
  })

const slice = createSlice({
  name: 'game',
  initialState: (): GameState => ({
    playerName: '',
    loadings: {
      createRoom: false,
      joinRoom: false,
    },
  }),
  reducers: {
    selectName(state, {payload}: Action<string> & { payload: string }) {
      state.playerName = payload
    },
    updateRoom(state, {payload}: Action<string> & { payload: Room }) {
      state.room = payload
    },
    changeLoading(state, {payload}: Action<string> & { payload: { loadingKey: keyof Loadings, value: boolean } }) {
      state.loadings[payload.loadingKey] = payload.value
    },
  },
})

export const actions = {...slice.actions, createRoom, joinRoom, assignQuiz, moveOn, vote}
export const reducer = slice.reducer
