import { App as GameApp } from './apps/game/App'
import { BrowserRouter, Route, Routes } from 'react-router-dom'

export const App = () =>
  <BrowserRouter>
    <Routes>
      <Route path="/" element={<GameApp mode={'player'}/>}/>
      <Route path="/host" element={<GameApp mode={'moderator'}/>}/>
    </Routes>
  </BrowserRouter>
