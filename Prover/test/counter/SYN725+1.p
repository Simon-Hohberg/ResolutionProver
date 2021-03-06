%--------------------------------------------------------------------------
% File     : SYN725+1 : TPTP v5.0.0. Released v2.5.0.
% Domain   : Syntactic
% Problem  : Peter Andrews Problem THM39
% Version  : Especial.
% English  :

% Refs     : [And97] Andrews (1994), Email to G. Sutcliffe
% Source   : [And97]
% Names    : THM39 [And97]

% Status   : CounterSatisfiable
% Rating   : 0.00 v4.1.0, 0.17 v4.0.1, 0.00 v3.1.0, 0.17 v2.6.0, 0.33 v2.5.0
% Syntax   : Number of formulae    :    1 (   0 unit)
%            Number of atoms       :    6 (   0 equality)
%            Maximal formula depth :    6 (   6 average)
%            Number of connectives :    5 (   0 ~  ;   1  |;   1  &)
%                                         (   1 <=>;   2 =>;   0 <=)
%                                         (   0 <~>;   0 ~|;   0 ~&)
%            Number of predicates  :    2 (   0 propositional; 1-1 arity)
%            Number of functors    :    0 (   0 constant; --- arity)
%            Number of variables   :    2 (   0 singleton;   2 !;   0 ?)
%            Maximal term depth    :    1 (   1 average)
% SPC      : FOF_CSA_EPR

% Comments :
%--------------------------------------------------------------------------
fof(thm39,conjecture,
    ( ! [X] :
        ( r(X)
        | ( s(X)
        <=> ( r(X)
            & s(X) ) ) )
   => ! [X] :
        ( r(X)
       => s(X) ) )).

%--------------------------------------------------------------------------
