 # ♟️ SICILIAN: <br> Smart Integrated Chess Information Logging in Algebraic Notation

SICILIAN is a mobile application that automatically records a chess game in algebraic notation. It also includes a built-in chess clock and provides a display of the moves being played, with an option to save the chess moves in your device. 

The application is written in Java and uses OpenCV 3.4.10.

# System Architecture

![SICILIAN - Poster](https://github.com/Resonance001/sicilian_chess_recognition/assets/60764656/3052afd5-e3ed-4543-b131-4c030e3f3e8e)

The setup requires two mobile devices, where one acts as the camera while the other acts as the chess clock. The camera is placed overhead and parallel to the chessboard while the clock is placed beside the chessboard. Both devices are connected through Bluetooth. 

# Images

### Camera Calibration

<p float="left">
  <img src="https://github.com/Resonance001/sicilian_chess_recognition/assets/60764656/bcdac7bf-b57a-44c8-bd3e-60fad7527702" width="250" />
  <img src="https://github.com/Resonance001/sicilian_chess_recognition/assets/60764656/921924ab-1dec-48de-b6a9-6db41512a78d" width="250" /> 
  <img src="https://github.com/Resonance001/sicilian_chess_recognition/assets/60764656/2fac2bda-7507-436d-a533-b6e0722a0793" width="250" />
</p>

### Chess Clock
<p float="left">
  <img src="https://github.com/Resonance001/sicilian_chess_recognition/assets/60764656/f1b3cbfc-17dd-4325-ae37-d4d4d4a16496" width="250" />
  <img src="https://github.com/Resonance001/sicilian_chess_recognition/assets/60764656/2de24c6b-66e8-413b-82e0-3d5954f8fd01" width="250" /> 
  <img src="https://github.com/Resonance001/sicilian_chess_recognition/assets/60764656/4fd6de2a-001b-4de7-98be-ed07867ab89a" width="250" />
</p>

# Improvements

- Making camera calibration adaptive eliminates the need for human intervention in setting up thresholds before commencing a game.
- Allowing 3D orientation will make device positioning more flexible and practical.
- Recognizing chess pieces allows for further features such as reading a specific positon or automatically detecting pawn promotion.
- The inclusion of a chess engine, such as Stockfish, can deter the need of third-party applications for analysis.

# Acknowledgements
This project is inspired by [ChessDetect](https://github.com/Elucidation/ChessDetect)
