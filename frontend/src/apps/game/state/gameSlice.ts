import { Action, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { server } from '../../../core/ws/GameServer'
import { Room } from '../../../domain/Room'

type Loadings = {
  createRoom: boolean;
  joinRoom: boolean;
}

type GameState = {
  playerName: string;
  mode: 'player' | 'moderator' | undefined;
  room?: Room;
  loadings: Loadings
}

const createRoom = createAsyncThunk<void, undefined>(
  'CREATE_ROOM',
  async (__, {dispatch}) => {
    dispatch(slice.actions.changeLoading({loadingKey: 'createRoom', value: true}))
    const room = await server.createRoom()
    dispatch(slice.actions.changeLoading({loadingKey: 'createRoom', value: false}))
    dispatch(slice.actions.selectMode('moderator'))
    dispatch(slice.actions.updateRoom(room))
    server.subscribeToRoomChanges(room.code, (room) => dispatch(slice.actions.updateRoom(room)))
  })

const joinRoom = createAsyncThunk<void, string>(
  'JOIN_ROOM',
  async (roomCode, {dispatch}) => {
    dispatch(slice.actions.changeLoading({loadingKey: 'joinRoom', value: true}))
    const room = await server.joinRoom(roomCode)
    dispatch(slice.actions.changeLoading({loadingKey: 'joinRoom', value: false}))
    dispatch(slice.actions.selectMode('player'))
    dispatch(slice.actions.updateRoom(room))
    server.subscribeToRoomChanges(roomCode, (room) => dispatch(slice.actions.updateRoom(room)))
  })

const slice = createSlice({
  name: 'game',
  initialState: (): GameState => ({
    playerName: '',
    mode: undefined,
    loadings: {
      createRoom: false,
      joinRoom: false,
    },
  }),
  reducers: {
    selectName(state, {payload}: Action<string> & { payload: string }) {
      state.playerName = payload
    },
    selectMode(state, {payload}: Action<string> & { payload: 'player' | 'moderator' }) {
      state.mode = payload
    },
    updateRoom(state, {payload}: Action<string> & { payload: Room }) {
      state.room = payload
    },
    changeLoading(state, {payload}: Action<string> & { payload: { loadingKey: keyof Loadings, value: boolean } }) {
      state.loadings[payload.loadingKey] = payload.value
    },
  },
})

export const actions = {...slice.actions, createRoom, joinRoom}
export const reducer = slice.reducer
