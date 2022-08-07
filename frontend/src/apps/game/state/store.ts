import { combineReducers, configureStore } from '@reduxjs/toolkit'
import { reducer as playerName } from './playerNameSlice'
import { reducer as moderator } from './moderatorSlice'
import { reducer as gameServer } from '../../../core/ws/gameServerSlice'

export const createStore = () => configureStore({
  reducer: combineReducers({
    gameServer,
    playerName,
    moderator,
  }),
})

export type State = ReturnType<ReturnType<typeof createStore>['getState']>;