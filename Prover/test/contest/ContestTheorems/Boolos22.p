fof(ax1,axiom,( ![N]: f(N,one) = s(one) )).
fof(ax2,axiom,( ![X]: f(one,s(X)) = s(s(f(one,X))) )).
fof(ax3,axiom,( ![N,X]: f(s(N),s(X)) = f(N,f(s(N),X)) )).
fof(ax4,axiom,( d(one) )).
fof(ax5,axiom,( ![X]: ( d(X) => d(s(X)) ) )).
fof(con,conjecture,( d(f(s(one),s(one))) )).


