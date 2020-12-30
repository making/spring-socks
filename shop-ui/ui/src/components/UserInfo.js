import React, {useEffect, useState} from "react";

export function UserInfo() {
    const [me, setMe] = useState({});
    useEffect(() => {
        fetchMe().then(setMe);
    }, []);
    if (me.family_name && me.given_name) {
        return <p>
            {`${me.family_name} ${me.given_name}`}<br/>
            <a href={"/logout"}>Logout</a>
        </p>;
    }
    return <p><a href={"/login"}>Login</a></p>;
}

function fetchMe() {
    return fetch('/me', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}