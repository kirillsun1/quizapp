import { Action, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { server } from '../../../core/ws/GameServer'
import { Room } from '../../../domain/Room'

type Loadings = {
  createRoom: boolean
  joinRoom: boolean
  voting: boolean
}

type GameState = {
  playerName: string
  room: Room | undefined
  loadings: Loadings
  currentAnswer: number | undefined
}

const createRoom = createAsyncThunk<void, undefined>(
  'CREATE_ROOM',
  async (__, {dispatch}) => {
    try {
      dispatch(slice.actions.changeLoading({loadingKey: 'createRoom', value: true}))
      const room = await server.createRoom()
      dispatch(slice.actions.updateRoom(room!))
      server.subscribeToRoomChanges(room!.code, (room) => dispatch(slice.actions.updateRoom(room)))
    } finally {
      dispatch(slice.actions.changeLoading({loadingKey: 'createRoom', value: false}))
    }
  })

const joinRoom = createAsyncThunk<void, { roomCode: string, onFailure: () => void }>(
  'JOIN_ROOM',
  async ({roomCode, onFailure}, {dispatch}) => {
    try {
      dispatch(slice.actions.changeLoading({loadingKey: 'joinRoom', value: true}))
      const room = await server.joinRoom(roomCode)
      server.subscribeToRoomChanges(roomCode, (room) => dispatch(slice.actions.updateRoom(room)))
      dispatch(slice.actions.updateRoom(room))
    } catch (e) {
      onFailure()
    } finally {
      dispatch(slice.actions.changeLoading({loadingKey: 'joinRoom', value: false}))
    }
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
  async (answer, {getState, dispatch}) => {
    try {
      dispatch(slice.actions.changeLoading({loadingKey: 'voting', value: true}))
      dispatch(slice.actions.selectAnswer(answer))
      await server.vote(getState().game.room!.code, answer)
    } finally {
      dispatch(slice.actions.changeLoading({loadingKey: 'voting', value: false}))
    }
  })

const slice = createSlice({
  name: 'game',
  initialState: (): GameState => ({
    playerName: '',
    room: undefined,
    loadings: {
      createRoom: false,
      joinRoom: false,
      voting: false,
    },
    currentAnswer: undefined,
  }),
  reducers: {
    selectName(state, {payload}: Action<string> & { payload: string }) {
      state.playerName = payload
    },
    updateRoom(state, {payload}: Action<string> & { payload: Room }) {
      if (state.room?.ongoingQuiz?.status !== payload.ongoingQuiz?.status) {
        state.currentAnswer = undefined
      }
      state.room = payload
    },
    changeLoading(state, {payload}: Action<string> & { payload: { loadingKey: keyof Loadings, value: boolean } }) {
      state.loadings[payload.loadingKey] = payload.value
    },
    selectAnswer(state, {payload}: Action<string> & { payload: number }) {
      state.currentAnswer = payload
    },
  },
})

export const actions = {...slice.actions, createRoom, joinRoom, assignQuiz, moveOn, vote}
export const reducer = slice.reducer
