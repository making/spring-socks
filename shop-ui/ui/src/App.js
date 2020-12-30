import React from 'react';
import {BrowserRouter as Router, Link, Route, Switch} from "react-router-dom";
import {Cart} from "./routes/Cart";
import {Sock} from "./routes/Sock";
import {Tag} from "./routes/Tag";
import {Home} from "./routes/Home";

export default function App() {
    return (
        <Router>
            <div>
                <h1>Spring Socks</h1>
                <nav>
                    <ul>
                        <li>
                            <Link to="/">Home</Link>
                        </li>
                        <li>
                            <Link to="/cart">Cart</Link>
                        </li>
                    </ul>
                </nav>
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

