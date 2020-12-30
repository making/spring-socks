import React, {useEffect, useState} from "react";
import {fetchSocksByTag} from "./Tag";
import {Link} from "react-router-dom";
import {Tags} from "../components/Tags";

export function Home() {
    const [socks, setSocks] = useState([]);
    useEffect(() => {
        fetchSocksByTag('featured', 1, 6).then(setSocks);
    }, []);
    return <div className="p-grid p-dir-rev">
        <div className="p-col-10">
            <h2>Spring Socks</h2>
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