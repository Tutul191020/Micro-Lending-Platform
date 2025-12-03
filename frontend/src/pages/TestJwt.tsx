import { useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';

export default function TestJwt() {
    const [result, setResult] = useState('Testing...');
    const [error, setError] = useState('');

    useEffect(() => {
        try {
            const token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
            const decoded = jwtDecode(token);
            setResult(JSON.stringify(decoded));
        } catch (err: any) {
            setError(err.message);
        }
    }, []);

    return (
        <div className="p-10">
            <h1>JWT Test</h1>
            <div id="jwt-result" className="bg-green-100 p-4 mb-4">{result}</div>
            <div id="jwt-error" className="bg-red-100 p-4">{error}</div>
        </div>
    );
}
