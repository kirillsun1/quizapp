import React from 'react'
import { GameScreen } from './GameScreen'
import { Provider } from 'react-redux'
import { createStore } from './state/store'

export function App() {
  return (
    <Provider store={createStore()}>
      <GameScreen/>
    </Provider>
  )
}