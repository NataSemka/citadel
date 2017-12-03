module Main exposing (..)

import Dashboard
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Login
import WebSocket


-- MAIN


main : Program Never Model Msg
main =
    Html.program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }



-- MODEL


type alias Model =
    { dashboard : Dashboard.Model
    , login : Login.Model
    , page : Page
    }


initModel : Model
initModel =
    { dashboard = Dashboard.initModel
    , login = Login.initModel
    , page = DashboardPage
    }


init : ( Model, Cmd Msg )
init =
    ( initModel
    , Cmd.none
    )


type Page
    = DashboardPage
    | LoginPage



-- UPDATE


type Msg
    = ChangePage Page
    | DashboardMsg Dashboard.Msg
    | LoginMsg Login.Msg
    | NewMessage String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NewMessage str ->
            ( model, Cmd.none )

        ChangePage page ->
            ( { model
                | page = page
              }
            , Cmd.none
            )

        DashboardMsg dashboardMsg ->
            ( { model
                | dashboard =
                    Dashboard.update dashboardMsg model.dashboard
              }
            , Cmd.none
            )

        LoginMsg loginMsg ->
            ( { model
                | login =
                    Login.update loginMsg model.login
              }
            , Cmd.none
            )



-- view


view : Model -> Html Msg
view model =
    let
        page =
            case model.page of
                DashboardPage ->
                    Html.map DashboardMsg
                        (Dashboard.view model.dashboard)

                LoginPage ->
                    Html.map LoginMsg
                        (Login.view model.login)
    in
    div []
        [ div []
            [ a [ onClick (ChangePage DashboardPage) ]
                [ text "Dashboard" ]
            , span [] [ text " | " ]
            , a [ onClick (ChangePage LoginPage) ]
                [ text "Log in" ]
            ]
        , hr [] []
        , page
        ]



-- SUCSCRIPTIONS


echoServer : String
echoServer =
    "ws://echo.websocket.org"


subscriptions : Model -> Sub Msg
subscriptions model =
    WebSocket.listen echoServer NewMessage
