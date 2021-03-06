package model

import org.scalatest._
import scala.collection.mutable.ArrayBuffer

class BoardTest extends FlatSpec {

  def createBoardTileListFromPositionsAndStrings(wantedBoardTiles: List[(Int, Int, String)]): List[BoardTile] =
    for(tuple <- wantedBoardTiles) yield BoardTileImpl(new PositionImpl(tuple._1,tuple._2), CardImpl(tuple._3))
  def addListOfCardsToTheBoard(board:Board, cards: List[(Card, Int, Int)]): Unit = for (card <- cards) board.addCard2Tile(card._1, card._2, card._3)

  // TEST SUI METODI UTILIZZATI PER INSERIRE E RIMUOVERE ELEMENTI DALLA BOARD
  "A card " should " be added to the board in a specific position" in {
    val card = CardImpl("A")
    val board = BoardImpl()
    board.addCard2Tile(card, 1,1 )
    assert(board.boardTiles.head.card == card)
  }
  "A card " should " be added to the board in a specific position and in the played word" in {
    val card = CardImpl("A")
    val board = BoardImpl()
    board.addCard2Tile(card, 1,1, add2PlayedWord = true)
    assert(board.boardTiles.head.card.equals(card) && board.playedWord.contains(BoardTileImpl(PositionImpl(1,1),card)))
  }
  "A list of cards " should " be added to the board and removed" in {
    val boardTile = BoardTileImpl(PositionImpl(1,3), CardImpl("D"))
    val boardTile1 = BoardTileImpl(PositionImpl(2,3), CardImpl("B"))
    val boardTile2 = BoardTileImpl(PositionImpl(3,3), CardImpl("E"))
    val listBoardTile = List(boardTile,boardTile1,boardTile2)
    val board = BoardImpl()
    board.addPlayedWord(listBoardTile)
    assert(board.playedWord.equals(listBoardTile))
  }
  "A list of cards " should " be removed from the board" in {
    val boardTile = BoardTileImpl(PositionImpl(1,3), CardImpl("D"))
    val boardTile1 = BoardTileImpl(PositionImpl(2,3), CardImpl("B"))
    val boardTile2 = BoardTileImpl(PositionImpl(3,3), CardImpl("E"))
    val listBoardTile = List(boardTile,boardTile1,boardTile2)
    val board = BoardImpl()
    board.addPlayedWord(listBoardTile)
    assert(board.playedWord.equals(listBoardTile))
    board.clearPlayedWords()
    assert(board.playedWord.isEmpty)
  }
  "A list of played cards " should " be remove from the board" in {
    val boardTile = BoardTileImpl(PositionImpl(1,3), CardImpl("D"))
    val listBoardTile = List(boardTile)
    val board = BoardImpl()
    board.addPlayedWord(listBoardTile)
    board.clearBoardFromPlayedWords()
    assert(!board.boardTiles.contains(boardTile))
  }

  // TEST SUI CONTROLLI EFFATUATI SULLA PRIMA PAROLA INSERITA
  "The first word" should "be on SCARABEO image on the board" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,9,"F"), (10,9,"I"))))
    assert(board.checkGameFirstWord())
  }
  // le prime lettere giocate devono essere adiacenti se no non possono formare una parola
  "The first letters played" should "be adjacent " in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,9,"F"), (10,9,"I"), (11, 9 , "C"))))
    assert(board.checkGameFirstWord())
    board.clearPlayedWords()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,9,"F"), (9,10,"I"), (9, 11 , "C"))))
    assert(board.checkGameFirstWord())
  }
  // il controllo precedente deve funzionare anche se le lettere non sono state giocate in ordine lessicografico
  "The first letters played" should "be adjacent: played not in order" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((11,9,"F"), (9,9,"I"), (10, 9 , "C"))))
    assert(board.checkGameFirstWord())
    board.clearPlayedWords()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,11,"F"), (9,9,"I"), (9, 10 , "C"))))
    assert(board.checkGameFirstWord())
  }
  "A validity check " should "be performed to check if the first played letters are adjacent " in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,9,"F"), (10,9,"I"), (15, 9 , "C"))))
    assert(!board.checkGameFirstWord())
    board.clearPlayedWords()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,9,"F"), (9,10,"I"), (9, 13 , "C"))))
    assert(!board.checkGameFirstWord())
  }
  // le lettere giocate devono essere sulla stessa colonna o sulla stessa riga, altrimenti sono in diagonale
  // e non devono passare il controllo
  "A validity check " should "be performed to check if the first played letters are adjacent: case 2 " in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,9,"F"), (10,10,"I"), (11, 11 , "C"))))
    assert(!board.checkGameFirstWord())
  }

  // TEST PER IL CONTROLLO DELLE PAROLE ESTRATTE DALLA BOARD
  // le lettere giocate devono essere o nella stessa colonna o nella stessa riga
  "A validity check " should "be performed to check if played letters are on the same column" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((1,1,"A"), (2,1,"A"))))
    assert(board.takeCardToCalculatePoints(isFirstWord = true).nonEmpty)
  }
  "A validity check " should "be performed to check if played letters are on the same on the same row " in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((1,3,"A"), (1,2,"A"))))
    assert(board.takeCardToCalculatePoints(isFirstWord = true).nonEmpty)
    board.clearBoardFromPlayedWords()
  }
  "A validity check " should "be performed to check if played letters aren't on the same column or on the same row " in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((1,1,"A"), (2,2,"A"))))
    assert(board.takeCardToCalculatePoints(isFirstWord = true).isEmpty)
  }
  // gli spazi fra le lettere inserite devono essere occupate da quelle nella board
  // per formare delle parole
  // lettere adicenti
  "The letters played " should "be adjacent to the letters in the board" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((9,9,"F"), (9,10,"I"))))
    assert(board.takeCardToCalculatePoints(isFirstWord = true).nonEmpty)
  }
  // lettere non adiacenti con caselle nella board già piene
  "Not adjacent letters" should "not make a word: horizontal" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((3,1,"C"), (3,4,"D"))))
    board.addCard2Tile(CardImpl("A"), 2, 3)
    board.addCard2Tile(CardImpl("B"), 3, 3)
    assert(board.takeCardToCalculatePoints(isFirstWord = true).isEmpty)
  }
  "Not adjacent letters" should "not make a word: vertical" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((2,3,"S"), (5,3,"I"))))
    board.addCard2Tile(CardImpl("S"), 3, 2)
    board.addCard2Tile(CardImpl("i"), 3, 3)
    assert(board.takeCardToCalculatePoints(isFirstWord = true).isEmpty)
  }
  // controllo delle parole ottenute dalla board
  // lettere maiuscole -> già presenti nella board
  // lettere minuscole -> giocate nel turno
  // 1 => controllo incroci se aggiunta una singola lettera
  // - B -
  // D e F
  // - G -
  "The list of words to be checked " should "be the ones in the cross created by the played letter" in {
    val board = BoardImpl()
    val listOfWords = List(List((CardImpl("B"),"DEFAULT"), (CardImpl("E"),"DEFAULT"), (CardImpl("G"),"2P")), List((CardImpl("D"),"2P"), (CardImpl("E"),"DEFAULT"), (CardImpl("F"),"DEFAULT")))
    addListOfCardsToTheBoard(board, List((CardImpl("B"), 1, 3), (CardImpl("D"), 2, 2), (CardImpl("F"), 2, 4), (CardImpl("G"), 3,3)))
    board.addPlayedWord(List(BoardTileImpl(PositionImpl(2,3), CardImpl("E"))))
    assert(board.takeCardToCalculatePoints(isFirstWord = true) == listOfWords)
  }
  // 2 => controllo incroci orizzontali
  // - A b C
  // - D e F
  "The list of words to be checked" should "contain also the horizontal crossings, if the played letters are in vertical" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((1,3,"B"),(2,3,"E"))))
    val listOfWords = List(List((CardImpl("D"),"2P"), (CardImpl("E"),"DEFAULT"), (CardImpl("F"),"DEFAULT")), List((CardImpl("B"),"DEFAULT"), (CardImpl("E"),"DEFAULT")), List((CardImpl("A"),"DEFAULT"), (CardImpl("B"),"DEFAULT"), (CardImpl("C"),"DEFAULT")))
    addListOfCardsToTheBoard(board, List((CardImpl("A"), 1, 2),(CardImpl("D"), 2, 2), (CardImpl("C"), 1,4), (CardImpl("F"), 2,4)))
    assert(board.takeCardToCalculatePoints(isFirstWord = true) == listOfWords)
  }
  // 3 => controllo incroci verticali
  // - A B
  // - c d
  // - E F
  "The list of words to be checked" should "contain also the vertical crossings, if the played letters are in horizontal" in {
    val board = BoardImpl()
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((2,2,"C"), (2,3,"D"))))
    val listOfWords = List(List((CardImpl("B"),"DEFAULT"), (CardImpl("D"),"DEFAULT"), (CardImpl("F"),"2P")), List((CardImpl("A"),"DEFAULT"), (CardImpl("C"),"2P"), (CardImpl("E"),"DEFAULT")), List((CardImpl("C"),"2P"), (CardImpl("D"),"DEFAULT")))
    addListOfCardsToTheBoard(board, List((CardImpl("A"), 1, 2), (CardImpl("B"), 1, 3), (CardImpl("E"), 3,2), (CardImpl("F"), 3,3)))
    assert(board.takeCardToCalculatePoints(isFirstWord = true) == listOfWords)
  }

  // TEST PER RICAVARE LA PAROLA NEL FORMATO DEL DIZIONARIO DALLE LETTERE GIOCATE
  "The word" should "be extracted from the list of letters" in {
    val board = BoardImpl()
    val listOfWords: List[List[(Card,String)]] = List(List((CardImpl("O"),"DEFAULT"), (CardImpl("C"),"2P"), (CardImpl("A"),"DEFAULT")))
    assert(board.getWordsFromLetters(listOfWords).contains("oca"))
  }

  // TEST PER IL CALCOLO DEL PUNTEGGIO DELLE PAROLE
  "The word points" should "assigned according to scarabeo rules" in {
    val board = BoardImpl()
    val aspectedPoints = 28
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((1,2,"F"), (2,2,"I"), (3,2,"C"), (4,2,"O"))))
    assert(board.calculateTurnPoints(board.takeCardToCalculatePoints(isFirstWord = true), isFirstWord = true) == aspectedPoints)
  }
  "The word points" should "be doubled for the first word" in {
    val board = BoardImpl()
    val aspectedPoints = 14
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((1,2,"F"), (2,2,"I"), (3,2,"C"), (4,2,"O"))))
    // calcolo punteggio prima parola
    assert(board.calculateTurnPoints(board.takeCardToCalculatePoints(isFirstWord = true), isFirstWord = true) == aspectedPoints*scoreConstants.firstWordBonus)
    // calcolo punteggio seconda parola
    assert(board.calculateTurnPoints(board.takeCardToCalculatePoints()) == aspectedPoints)
  }
  "The word 'SCARABEO'" should "have a bonus" in {
    val board = BoardImpl()
    val aspectedPoints = 112
    board.addPlayedWord(createBoardTileListFromPositionsAndStrings(List((1,2,"S"), (2,2,"C"), (3,2,"A"), (4,2,"R"), (5,2,"A"), (6,2,"B"), (7,2,"E"), (8,2,"O"))))
    assert(board.calculateTurnPoints(board.takeCardToCalculatePoints(isFirstWord = true), isFirstWord = true) == aspectedPoints+scoreConstants.bonusScarabeoWord)
  }
}
