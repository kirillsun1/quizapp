import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { server } from './GameServer'

export interface State {
  connecting: boolean;
  connected: boolean;
  error: boolean;
}

const createState: () => State = () => ({
  connecting: false,
  connected: false,
  error: false,
})

const attemptConnection = createAsyncThunk<void, string>(
  'GAME_SERVER',
  async (playerName, _) => {
    await server.connect(playerName)
  })

const gameServerSlice = createSlice({
  name: 'gameServer',
  initialState: createState,
  reducers: {},
  extraReducers: builder => {
    builder
      .addCase(attemptConnection.pending, state => {
        state.connected = false
        state.error = false
        state.connecting = true
      })
      .addCase(attemptConnection.fulfilled, state => {
        state.connected = true
        state.error = false
        state.connecting = false
      })
      .addCase(attemptConnection.rejected, state => {
        state.connected = false
        state.error = true
        state.connecting = false
      })
  },
})

export { attemptConnection }
export const reducer = gameServerSlice.reducer