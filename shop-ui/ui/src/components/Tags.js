import React, {useEffect, useState} from "react";
import {Link} from "react-router-dom";

export function Tags() {
    const [tags, setTags] = useState({tags: []});
    useEffect(() => {
        fetchTags().then(setTags);
    }, []);
    return <div>
        <h3>Tags</h3>
        <ul>
            {tags.tags.map(tag => <li key={tag}><Link
                to={`/tags/${tag}`}>{tag}</Link></li>)}
        </ul>
    </div>;
}

function fetchTags() {
    return fetch('/tags', {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        },
    })
        .then(res => res.json());
}