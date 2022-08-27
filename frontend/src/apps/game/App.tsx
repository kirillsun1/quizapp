import React from 'react'
import { GameScreen } from './GameScreen'
import { Provider } from 'react-redux'
import { createStore } from './state/store'

export const App = ({mode}: { mode: 'player' | 'moderator' }) =>
  <Provider store={createStore()}>
    <GameScreen mode={mode}/>
  </Provider>