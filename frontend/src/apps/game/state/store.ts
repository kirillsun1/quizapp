import { combineReducers, configureStore } from '@reduxjs/toolkit'
import { reducer as game } from './gameSlice'
import { reducer as gameServer } from '../../../core/ws/gameServerSlice'

export const createStore = () => configureStore({
  reducer: combineReducers({
    gameServer,
    game,
  }),
})

export type State = ReturnType<ReturnType<typeof createStore>['getState']>;