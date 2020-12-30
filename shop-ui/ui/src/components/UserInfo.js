import React, {useEffect, useState} from "react";

export function UserInfo() {
    const [me, setMe] = useState({});
    useEffect(() => {
        fetchMe().then(setMe);
    }, []);
    if (me.lastName && me.firstName) {
        return <div className="p-d-flex p-jc-end">
            <p>
                {`${me.lastName} ${me.firstName}`}<br/>
                <a href={"/logout"}>Logout</a>
            </p>
        </div>;
    }
    return <div className="p-d-flex p-jc-end">
        <p><a href={"/login"}>Login</a></p>
    </div>;
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