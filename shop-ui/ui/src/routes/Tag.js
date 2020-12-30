import {Link, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import {Tags} from "../components/Tags";

export function Tag() {
    const {tag} = useParams();
    const [socks, setSocks] = useState([]);
    useEffect(() => {
        fetchSocksByTag(tag).then(setSocks);
    }, [tag]);
    return <div className="p-grid p-dir-rev">
        <div className="p-col-10">
            <h2>Tag: {tag}</h2>
            <ul>
                {socks.map(sock => <li key={sock.id}><Link
                    to={`/details/${sock.id}`}>{sock.name}</Link></li>)}
            </ul>
        </div>
        <div className="p-col-2">
            <Tags/>
        </div>
    </div>;
}

export function fetchSocksByTag(tag, page, size) {
    return fetch(`/tags/${tag}?page=${page || 1}&size=${size || 10}`, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}