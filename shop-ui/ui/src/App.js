import React from 'react';
import {BrowserRouter as Router, Link, Route, Switch} from "react-router-dom";
import {Cart} from "./routes/Cart";
import {Sock} from "./routes/Sock";
import {Tag} from "./routes/Tag";
import {Home} from "./routes/Home";
import {CartSummary} from "./components/CartSummary";
import {UserInfo} from "./components/UserInfo";

export default function App() {
    return (
        <Router>
            <div>
                <h1><Link to="/">Spring Socks</Link></h1>
                <UserInfo/>
                <CartSummary/>
                <Switch>
                    <Route path="/cart">
                        <Cart/>
                    </Route>
                    <Route exact path="/details/:id">
                        <Sock/>
                    </Route>
                    <Route exact path="/tags/:tag">
                        <Tag/>
                    </Route>
                    <Route path="/">
                        <Home/>
                    </Route>
                </Switch>
            </div>
        </Router>
    );
}

