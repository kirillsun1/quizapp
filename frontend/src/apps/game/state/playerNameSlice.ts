import { Action, createSlice } from '@reduxjs/toolkit'

export interface PlayerNameState {
  value: string | undefined;
}

type AssignNameAction = Action<string> & {
  payload: string
};

const createInitialState: () => PlayerNameState = () => ({value: undefined})
const playerNameSlice = createSlice({
  name: 'playerName',
  initialState: createInitialState,
  reducers: {
    assignPlayerName(state, action: AssignNameAction) {
      state.value = action.payload
    },
  },
})

export const {assignPlayerName} = playerNameSlice.actions
export const reducer = playerNameSlice.reducer
