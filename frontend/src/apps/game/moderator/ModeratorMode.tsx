import React, { useState } from 'react'
import {
  QAlert,
  QAlertIcon,
  QBox,
  QButton,
  QCenter,
  QCircularProgress,
  QContainer,
  QDrawer,
  QDrawerBody,
  QDrawerContent,
  QDrawerOverlay,
  QFlex,
  QHeading,
  QInput,
  QListItem,
  QSimpleGrid,
  QStack,
  QText,
  QUnorderedList,
  QVStack,
  useDisclosure,
} from '../../../core/components'
import { useDispatch, useSelector } from 'react-redux'
import { actions } from '../state/gameSlice'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import { State } from '../state/store'
import { OngoingQuizStatus } from '../../../domain/Room'

export function ModeratorMode() {
  const room = useSelector((state: State) => state.game.room)

  return (
    <QContainer maxW={'container.lg'} minH={'100vh'}>
      {
        !room ? <CreateRoom/> : <InRoom/>
      }
    </QContainer>
  )
}

function CreateRoom() {
  const createRoomInProgress = useSelector((state: State) => state.game.loadings.createRoom)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const createRoom = () => {
    dispatch(actions.createRoom())
  }

  return (
    <QCenter minH={'100vh'}>
      <QBox
        background={'white'}
        padding={16}
        borderRadius={36}
        boxShadow="xl"
      >
        {
          createRoomInProgress
            ? (
              <QVStack>
                <QCircularProgress isIndeterminate/>
                <QText>Creating room...</QText>
              </QVStack>
            )
            : (
              <QButton size={'lg'} onClick={createRoom}>
                Create Room
              </QButton>
            )
        }
      </QBox>
    </QCenter>
  )
}

function InRoom() {
  const room = useSelector((state: State) => state.game.room!)
  const {isOpen, onOpen, onClose} = useDisclosure()

  return (
    <QStack paddingTop={12} gap={6}>
      <QBox
        background={'white'}
        paddingY={8}
        paddingX={16}
        borderRadius={36}
        boxShadow="xl"
      >
        <QFlex
          direction={'row'}
          justifyContent={'space-between'}
          alignItems={'center'}
          wrap={'wrap'}
        >
          <QHeading>
            Room Moderation
          </QHeading>
          <QFlex alignItems={'center'}>
            <QBox mr={12}>
              <QText fontWeight="bold">{room.code}</QText>
              <QText fontSize="sm">Room code</QText>
            </QBox>
            <QBox mr={12}>
              <QText fontWeight="bold">{room.players.length}</QText>
              <QText fontSize="sm">Player(s)</QText>
            </QBox>
            <QButton onClick={onOpen} disabled={room.players.length === 0}>
              See Players
            </QButton>
          </QFlex>
        </QFlex>
      </QBox>

      <QDrawer placement={'right'} onClose={onClose} isOpen={isOpen}>
        <QDrawerOverlay/>
        <QDrawerContent>
          <QDrawerBody paddingTop={8}>
            <PlayersPoints players={room.players} points={room.ongoingQuiz?.points ?? {}}/>
          </QDrawerBody>
        </QDrawerContent>
      </QDrawer>

      <QBox
        background={'white'}
        paddingY={8}
        paddingX={16}
        borderRadius={36}
        boxShadow="xl"
      >
        <QSimpleGrid columns={2} spacingX={8}>
          <QuizSetup/>
          <StartGame/>
        </QSimpleGrid>
      </QBox>

      <RoundInProgress/>

      <RoundFinished/>
    </QStack>
  )
}

function PlayersPoints({players, points}: { players: string[], points: { [player: string]: number } }) {
  return (
    <QUnorderedList>
      {
        players.map(p =>
          <QListItem key={p}>
            {p} {points[p] ? `(${points[p]} points)` : null}
          </QListItem>)
      }
    </QUnorderedList>
  )
}

function QuizSetup() {
  const quiz = useSelector((state: State) => state.game.room?.ongoingQuiz)
  const [quizId, setQuizId] = useState<string>('')
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const assignQuiz = () => {
    dispatch(actions.assignQuiz(parseInt(quizId)))
  }

  if ((quiz?.status ?? OngoingQuizStatus.NOT_STARTED) !== OngoingQuizStatus.NOT_STARTED) {
    return null
  }

  return (
    <QStack marginBottom={6}>
      <QHeading marginTop={6} marginBottom={1} size={'md'}>
        Select Quiz:
      </QHeading>
      <QStack maxW={'md'} direction={'row'}>
        <QInput value={quizId}
                placeholder={'Quiz ID'}
                type={'number'}
                disabled={!!quiz}
                onChange={event => setQuizId(event.target.value)}/>
        {
          !quiz ? <QButton onClick={() => assignQuiz()}>Ok</QButton> : null
        }
      </QStack>
    </QStack>
  )
}

function StartGame() {
  const room = useSelector((state: State) => state.game.room!)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const start = () => {
    dispatch(actions.moveOn())
  }

  const quizStatus = room.ongoingQuiz?.status ?? OngoingQuizStatus.NOT_STARTED
  const quizAssigned = !!room.ongoingQuiz
  const haveEnoughPlayers = room.players.length > 0
  const canStartQuiz = quizAssigned && haveEnoughPlayers

  if (quizStatus !== OngoingQuizStatus.NOT_STARTED) {
    return null
  }

  return (
    <QStack>
      {
        !quizAssigned
          ? (
            <QAlert status="warning">
              <QAlertIcon/>
              While players are joining, you need to select a quiz.
            </QAlert>
          )
          : (
            <QAlert status="success">
              <QAlertIcon/>
              Quiz selected!
            </QAlert>
          )
      }

      {
        !haveEnoughPlayers
          ? (
            <QAlert status="warning">
              <QAlertIcon/>
              You don't have enough players to start.
            </QAlert>
          )
          : (
            <QAlert status="success">
              <QAlertIcon/>
              You have enough players to start.
            </QAlert>
          )
      }

      <QButton size={'lg'} onClick={start} disabled={!canStartQuiz}>Start</QButton>
    </QStack>
  )
}

function RoundInProgress() {
  const quiz = useSelector((state: State) => state.game.room?.ongoingQuiz)
  const status = quiz?.status
  const question = quiz?.currentQuestion
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const finishRound = () => {
    dispatch(actions.moveOn())
  }

  if (status !== OngoingQuizStatus.QUESTION_IN_PROGRESS || !question) {
    return null
  }

  return (
    <QBox
      padding={4}
      borderRadius={4}
      color={'darkgreen'}
      background={'green.100'}>
      <QHeading size={'md'}>Question in progress</QHeading>
      <QBox padding={4} borderRadius={4} background={'gray.50'} marginY={4}>
        <QHeading size={'xs'}>{question.text}</QHeading>
      </QBox>
      <QHeading size={'xs'} marginBottom={4}>Give players some time to answer.</QHeading>
      <QButton size={'lg'} onClick={finishRound}>Finish round</QButton>
    </QBox>
  )
}

function RoundFinished() {
  const room = useSelector((state: State) => state.game.room)
  const quiz = room?.ongoingQuiz
  const status = quiz?.status
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const proceed = () => {
    dispatch(actions.moveOn())
  }

  if (!room || !quiz) {
    return null
  }
  if (status !== OngoingQuizStatus.WAITING && status !== OngoingQuizStatus.DONE) {
    return null
  }

  return (
    <QStack>
      <QHeading size={'md'}>Round Finished!</QHeading>
      <QSimpleGrid columns={2}>
        <PlayersPoints players={room.players} points={quiz.points}/>
        {
          status !== OngoingQuizStatus.DONE
            ? <QButton size={'lg'} onClick={proceed}>To the next round!</QButton>
            : null
        }
      </QSimpleGrid>
    </QStack>
  )
}