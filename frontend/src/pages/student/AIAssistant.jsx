import { useRef, useState } from 'react';
import { Send, Sparkles } from 'lucide-react';
import { getRequestError, sendAssistantMessage } from '../../services/studentService';

const prompts = [
  'How do I apply for a job on this website?',
  'What skills should I learn for Java development?',
  'How should I prepare for an interview?',
  'How can I improve my resume?',
  'What projects should I build?',
];

export function AIAssistant() {
  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState('');
  const listRef = useRef(null);

  async function send(text = message) {
    const question = text.trim();
    if (!question || sending) return;
    setError('');
    setMessage('');
    const userMessage = { role: 'user', content: question, time: new Date() };
    setMessages((current) => [...current, userMessage]);
    setSending(true);
    try {
      const history = messages.slice(-8).map(({ role, content }) => ({ role: role.toUpperCase(), content }));
      const result = await sendAssistantMessage(question, history);
      setMessages((current) => [...current, {
        role: 'assistant',
        content: result.response,
        generatedByProvider: result.generatedByProvider,
        time: new Date(),
      }]);
      requestAnimationFrame(() => listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' }));
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not reach the career assistant.'));
    } finally {
      setSending(false);
    }
  }

  return (
    <section className="student-page assistant-page">
      <header className="student-page-header"><div><p className="eyebrow">PERSONALIZED CAREER GUIDANCE</p><h1>AI Career Assistant</h1>
        <p>Ask in your language about this website, your career, or general questions.</p></div></header>
      <div className="assistant-card">
        <div className="assistant-chat" ref={listRef} aria-live="polite">
          {messages.length === 0 && <div className="assistant-welcome"><span className="public-feature-icon"><Sparkles size={20} /></span>
            <h2>What would you like to work on?</h2><p>Ask about using Northstar, career guidance, or another topic. The assistant follows your language.</p>
            <div className="prompt-list">{prompts.map((prompt) => <button type="button" key={prompt} disabled={sending} onClick={() => send(prompt)}>{prompt}</button>)}</div>
          </div>}
          {messages.map((item, index) => <article className={`chat-message ${item.role}`} key={`${item.time.getTime()}-${index}`}>
            <span className="chat-speaker">{item.role === 'user' ? 'You' : 'Career assistant'}</span>
            <p>{item.content}</p>
            {item.role === 'assistant' && !item.generatedByProvider && <small>Local guidance · Configure an AI API key for personalized provider responses.</small>}
            <time>{item.time.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}</time>
          </article>)}
          {sending && <p className="assistant-typing" role="status">Preparing guidance…</p>}
        </div>
        {error && <p className="form-error assistant-error" role="alert">{error}</p>}
        <form className="assistant-input" onSubmit={(event) => { event.preventDefault(); send(); }}>
          <label className="visually-hidden" htmlFor="assistant-message">Ask the career assistant</label>
          <input id="assistant-message" maxLength="2000" value={message} onChange={(event) => setMessage(event.target.value)}
            placeholder="Ask about this website or anything else…" disabled={sending} />
          <button type="submit" aria-label="Send message" disabled={sending || !message.trim()}><Send size={17} /></button>
        </form>
      </div>
    </section>
  );
}
