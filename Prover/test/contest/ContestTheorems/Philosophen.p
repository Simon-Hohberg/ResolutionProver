% Platon hatte Recht mit seiner Einschaetzung des Sokrates genau dann,
% wenn Sokrates kein großer Philosoph war.
fof(ax1,axiom,
    ( ( platonRechtMitEinschSokrates => sokratesKeinGrosserPhilosoph ) & ( platonRechtMitEinschSokrates <= sokratesKeinGrosserPhilosoph ) )).

% Wenn Sokrates ein großer Philosoph war, dann hatte Aristoteles Recht
% mit seiner Einschaetzung des Platon.
fof(ax2,axiom,
    ( sokratesKeinGrosserPhilosoph => aristotelesRechtMitEinschPlaton )).

% Aristoteles hatte nur dann Recht mit seiner Einschaetzung des Platon,
% falls Platon Recht hatte mit seiner Einschaetzung des Sokrates.
fof(ax3,axiom,
    ( ( sokratesKeinGrosserPhilosoph => aristotelesRechtMitEinschPlaton ) &     ( sokratesKeinGrosserPhilosoph <= aristotelesRechtMitEinschPlaton ) )).

% Platons Einschaetzung des Sokrates gilt.
fof(ax4,axiom, ( platonRechtMitEinschSokrates )).

% Sokrates war kein großer Philosoph.
fof(con,conjecture,
    ( sokratesKeinGrosserPhilosoph )).