module Main exposing (..)

import Dashboard
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Login


-- MODEL


type alias Model =
    { page : Page
    , dashboard : Dashboard.Model
    , login : Login.Model
    }


initModel : Model
initModel =
    { page = DashboardPage
    , dashboard = Dashboard.initModel
    , login = Login.initModel
    }


type Page
    = DashboardPage
    | LoginPage



-- UPDATE


type Msg
    = ChangePage Page
    | DashboardMsg Dashboard.Msg
    | LoginMsg Login.Msg


update : Msg -> Model -> Model
update msg model =
    case msg of
        ChangePage page ->
            { model
                | page = page
            }

        DashboardMsg dashboardMsg ->
            { model
                | dashboard =
                    Dashboard.update dashboardMsg model.dashboard
            }

        LoginMsg loginMsg ->
            { model
                | login =
                    Login.update loginMsg model.login
            }



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
            [ a
                [ href "#"
                , onClick (ChangePage DashboardPage)
                ]
                [ text "Dashboard" ]
            , span [] [ text " | " ]
            , a
                [ href "#"
                , onClick (ChangePage LoginPage)
                ]
                [ text "Log in" ]
            ]
        , hr [] []
        , page
        ]



-- MAIN


main : Program Never Model Msg
main =
    Html.beginnerProgram
        { model = initModel
        , view = view
        , update = update
        }
