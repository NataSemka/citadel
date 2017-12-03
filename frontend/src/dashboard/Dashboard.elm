module Dashboard exposing (..)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)


-- model


type alias Model =
    { players : List Player
    , plays : List Play
    }


type alias Player =
    { name : String
    , id : Int
    }


type alias Play =
    { name : String
    , id : Int
    }


initModel : Model
initModel =
    { players = []
    , plays = []
    }



-- update


type Msg
    = DashboardComponent


update : Msg -> Model -> Model
update msg model =
    model



-- view


view : Model -> Html Msg
view model =
    div []
        [ h3 []
            [ text "Dashboard page" ]
        , input
            [ type_ "text" ]
            []
        ]
