import React, {useEffect, useState} from "react";
import {BrowserRouter as Router, Link, Route, Switch} from "react-router-dom";
import {Card} from 'primereact/card';
import {Cart} from "./routes/Cart";
import {Sock} from "./routes/Sock";
import {Tag} from "./routes/Tag";
import {Home} from "./routes/Home";
import {CartSummary} from "./components/CartSummary";
import {UserInfo} from "./components/UserInfo";
import 'primereact/resources/themes/saga-green/theme.css';
import 'primereact/resources/primereact.min.css';
import 'primeicons/primeicons.css';
import 'primeflex/primeflex.css';

export default function App() {
    const [cart, setCart] = useState({
        items: []
    });
    const refreshCart = () => fetchCart().then(setCart);
    useEffect(refreshCart, []);
    return (
        <Router>
            <div>
                <Card style={{margin: '20px'}}>
                    <h1><Link to="/"><img src={"/img/logo.png"}
                                          alt={"Spring Socks"}/></Link>
                    </h1>
                    <UserInfo/>
                    <CartSummary cart={cart}/>
                    <Switch>
                        <Route path="/cart">
                            <Cart/>
                        </Route>
                        <Route exact path="/details/:id">
                            <Sock refreshCart={refreshCart}/>
                        </Route>
                        <Route exact path="/tags/:tag">
                            <Tag/>
                        </Route>
                        <Route path="/">
                            <Home/>
                        </Route>
                    </Switch>
                </Card>
            </div>
        </Router>
    );
}

function fetchCart() {
    return fetch('/cart', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}

