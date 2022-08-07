import { Action, createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { server } from '../../../core/ws/GameServer'

export interface ModeratorState {
  roomCode: string | undefined;
}

type AssignRoomAction = Action<string> & {
  payload: string
};

const createRoom = createAsyncThunk<void, undefined>(
  'CREATE_ROOM',
  async (__, {dispatch}) => {
    const roomCode = await server.createRoom()
    dispatch(moderatorSlice.actions.assignRoom(roomCode))
  })

const createInitialState: () => ModeratorState = () => ({roomCode: undefined})
const moderatorSlice = createSlice({
  name: 'moderator',
  initialState: createInitialState,
  reducers: {
    assignRoom(state, action: AssignRoomAction) {
      state.roomCode = action.payload
      console.log('assign ', action.payload)
    },
  },
})

export { createRoom }
export const {assignRoom} = moderatorSlice.actions
export const reducer = moderatorSlice.reducer
