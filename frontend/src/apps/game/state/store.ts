import { combineReducers, configureStore } from '@reduxjs/toolkit'
import { reducer as playerNameReducer } from './playerNameSlice'

export const createStore = () => configureStore({
  reducer: combineReducers({
    playerName: playerNameReducer,
  }),
})

export type State = ReturnType<ReturnType<typeof createStore>['getState']>;