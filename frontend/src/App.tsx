import { useState } from 'react'
import { QButton } from "./core/components";

function App() {
  const [count, setCount] = useState(0)

  return (
    <div className="App">
        <QButton background={'red'} onClick={() => setCount((count) => count + 1)}>
          count is {count}
        </QButton>
    </div>
  )
}

export default App
