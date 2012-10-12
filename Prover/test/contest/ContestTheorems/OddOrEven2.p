fof(ax1,axiom,( ?[X]: ( odd(X) | even(X) ) )).
fof(ax2,axiom,( ![X]: ( ( odd(X) => even(s(X)) ) & ( even(X) => odd(s(X)) ) ) )).

fof(con,conjecture,( ?[X]: ( ( odd(s(s(s(X)))) & even(s(s(s(s(X))))) ) ) ) ).