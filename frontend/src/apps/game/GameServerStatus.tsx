import React, { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import type { State } from './state/store'
import { QBadge, QBox, QButton, QCircularProgress, QStack, QText } from '../../core/components'
import { actions } from './state/gameSlice'
import { OngoingQuizStatus, Room } from '../../domain/Room'

type Props = {
  children?: React.ReactNode;
}

export const GameServerStatus = ({children}: Props) => (
  <>
    <DebugBox/>
    {children}
  </>
)

function DebugBox() {
  const [show, setShow] = useState(false)

  return (
    import.meta.env.MODE === 'development'
      ? (
        <QBox
          position={'absolute'}
          background={'white'}
          maxWidth={'2xl'}
          borderRadius={16}
          top={'5px'}
          left={'5px'}
          opacity={'0.85'}
        >
          <QButton size={'xs'} onClick={() => setShow(!show)}>Show debug</QButton>

          {
            show ? (
                <QStack margin={4}>
                  <QBox mr={12}>
                    <QText>
                      Server status
                      <ServerStatusBadge/>
                    </QText>
                  </QBox>
                  <StateChangers/>
                </QStack>
              )
              : null
          }
        </QBox>
      )
      : null
  )
}

function ServerStatusBadge() {
  const status = useSelector((state: State) => state.gameServer)
  const props = {
    ml: 1,
    mt: -1,
  }
  if (status.error) {
    return <QBadge colorScheme="red" {...props}>Error</QBadge>
  }
  if (status.connecting) {
    return <QCircularProgress size={'30px'} isIndeterminate {...props}/>
  }
  if (status.connected) {
    return <QBadge colorScheme="green" {...props}>Connected</QBadge>
  }
  return <QBadge colorScheme={'gray'} {...props}>Not connected</QBadge>
}

function StateChangers() {
  const dispatch = useDispatch()
  const states: { [state: string]: Room } = {
    WAITING_NO_PLAYERS: {
      code: 'TESTCODE',
      moderator: 'Somebody',
      players: [],
      ongoingQuiz: undefined,
    },
    WAITING_WITH_PLAYERS: {
      code: 'TESTCODE',
      moderator: 'Somebody',
      players: ['Some player', 'Another Player'],
      ongoingQuiz: undefined,
    },
    QUIZ_SET_NO_PLAYERS: {
      code: 'TESTCODE',
      moderator: 'Somebody',
      players: [],
      ongoingQuiz: {
        status: OngoingQuizStatus.NOT_STARTED,
        currentQuestion: undefined,
        points: {},
      },
    },
    QUIZ_SET_WITH_PLAYERS: {
      code: 'TESTCODE',
      moderator: 'Somebody',
      players: ['Some player', 'Another Player'],
      ongoingQuiz: {
        status: OngoingQuizStatus.NOT_STARTED,
        currentQuestion: undefined,
        points: {
          'Some player': 0,
          'Another Player': 0,
        },
      },
    },
    QUESTION_IN_PROGRESS: {
      code: 'TESTCODE',
      moderator: 'Somebody',
      players: ['Some player', 'Another Player'],
      ongoingQuiz: {
        status: OngoingQuizStatus.QUESTION_IN_PROGRESS,
        currentQuestion: {
          text: 'Is there a question?',
          answers: ['Probably', 'I don\'t think so'],
        },
        points: {
          'Some player': 0,
          'Another Player': 0,
        },
      },
    },
    FINISHED_ROUND: {
      code: 'TESTCODE',
      moderator: 'Somebody',
      players: ['Some player', 'Another Player'],
      ongoingQuiz: {
        status: OngoingQuizStatus.WAITING,
        currentQuestion: undefined,
        points: {
          'Some player': 100,
          'Another Player': 11,
        },
      },
    },
    FINISHED_QUIZ: {
      code: 'TESTCODE',
      moderator: 'Somebody',
      players: ['Some player', 'Another Player'],
      ongoingQuiz: {
        status: OngoingQuizStatus.DONE,
        currentQuestion: undefined,
        points: {
          'Some player': 38283,
          'Another Player': 2354123,
        },
      },
    },
  }

  const setState = (state: keyof typeof states) => {
    dispatch(actions.updateRoom(states[state]))
  }

  return (
    <QStack>
      {
        Object.keys(states)
          .map(state => (
            <QButton key={state}
                     size={'xs'}
                     variant="ghost"
                     onClick={() => setState(state)}>
              {state}
            </QButton>
          ))
      }
    </QStack>
  )
}