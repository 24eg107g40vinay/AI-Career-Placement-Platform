import { ArrowRight, BriefcaseBusiness, Compass, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';

const features = [
  {
    icon: Compass,
    title: 'Know your next step',
    description: 'Turn your skills and goals into a clear, practical career plan.',
  },
  {
    icon: BriefcaseBusiness,
    title: 'Find a better-fit role',
    description: 'Explore opportunities and understand how your experience matches.',
  },
  {
    icon: Sparkles,
    title: 'Prepare with confidence',
    description: 'Get guidance for skill gaps, interviews, and career decisions.',
  },
];

export function Home() {
  return (
    <main className="public-page">
      <header className="public-nav">
        <Link className="public-brand" to="/">Northstar</Link>
        <nav aria-label="Main navigation">
          <Link to="/jobs">Find jobs</Link>
          <Link to="/login">Sign in</Link>
          <Link className="public-nav-cta" to="/register">Get started</Link>
        </nav>
      </header>

      <section className="public-hero">
        <p className="eyebrow">CAREER INTELLIGENCE FOR WHAT'S NEXT</p>
        <h1>Build Your Career.<br /><span>Find Your Opportunity.</span></h1>
        <p className="public-hero-copy">
          A clearer path from the skills you have to the opportunities you want.
          Discover roles, strengthen your profile, and prepare for your next move.
        </p>
        <div className="public-hero-actions">
          <Link className="primary-button" to="/jobs">Find jobs <ArrowRight size={16} /></Link>
          <Link className="public-nav-cta" to="/register">Get started</Link>
          <Link className="public-secondary-action" to="/login">I already have an account</Link>
        </div>
      </section>

      <section className="public-features" aria-labelledby="features-heading">
        <div className="public-section-heading">
          <p className="eyebrow">YOUR CAREER, WITH MOMENTUM</p>
          <h2 id="features-heading">Make your next move a confident one.</h2>
        </div>
        <div className="public-feature-grid">
          {features.map(({ icon: Icon, title, description }) => (
            <article className="public-feature-card" key={title}>
              <span className="public-feature-icon"><Icon size={20} aria-hidden="true" /></span>
              <h3>{title}</h3>
              <p>{description}</p>
            </article>
          ))}
        </div>
      </section>

      <footer className="public-footer">
        <span>Northstar Career Intelligence</span>
        <span>Built to help you grow into your next opportunity.</span>
      </footer>
    </main>
  );
}
