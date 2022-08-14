import React from 'react'
import ReactDOM from 'react-dom/client'
import { App } from './App'
import { ChakraProvider, extendTheme, withDefaultColorScheme } from '@chakra-ui/react'

const theme = extendTheme(
  withDefaultColorScheme({
    colorScheme: 'green',
  }),
)

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <ChakraProvider theme={theme}>
      <App/>
    </ChakraProvider>
  </React.StrictMode>,
)
