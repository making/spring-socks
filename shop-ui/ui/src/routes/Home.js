import React, {useEffect, useState} from "react";
import {Carousel} from 'primereact/carousel';
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
            <h2>WE LOVE SOCKS!</h2>
            <p>
                100% SATISFACTION GUARANTEED<br/>
                <img src={'/img/spring_socks_1.jpg'} alt={"Spring Socks"}
                     style={{
                         height: '430px',
                         display: 'flex',
                         alignItems: 'center',
                     }}
                /><br/>
                Socks were invented by woolly mammoths to keep warm. They died out because
                stupid humans had to cut their legs off to get their socks
            </p>
            <h2>Featured Socks</h2>
            <Carousel value={socks} itemTemplate={itemTemplate} numVisible={3}
                      numScroll={1}
                      autoplayInterval={3000}></Carousel>
        </div>
        <div className="p-col-2">
            <Tags/>
        </div>
    </div>;
}

function itemTemplate(sock) {
    return <p>
        <Link to={`/details/${sock.id}`}>
            <img alt={sock.name} src={sock.imageUrl && sock.imageUrl[0]}
                 height={'270px'}/><br/>
            {sock.name}</Link>
    </p>;
}