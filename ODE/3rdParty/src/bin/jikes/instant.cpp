// Generate explicit instantiations for gendep.

#include "ast.h"

#ifdef USE_PRAGMA_FOR_TEMPINST
#pragma define (AstArray< AstImportDeclaration* >)
#pragma define (AstArray< AstVariableDeclarator* >)
#pragma define (AstArray< AstMethodDeclaration* >)
#pragma define (AstArray< Ast* >)
#pragma define (AstArray< AstExpressionStatement* >)
#pragma define (AstArray< AstStaticInitializer* >)
#pragma define (AstArray< AstBlock* >)
#pragma define (AstArray< AstStatement* >)
#pragma define (AstArray< AstConstructorDeclaration* >)
#pragma define (AstArray< AstBrackets* >)
#pragma define (AstArray< AstInterfaceDeclaration* >)
#pragma define (AstArray< AstCatchClause* >)
#pragma define (AstArray< AstClassDeclaration* >)
#pragma define (AstArray< int >)
#pragma define (AstArray< AstFieldDeclaration* >)
#pragma define (AstArray< AstDimExpr* >)
#pragma define (AstArray< CaseElement* >)
#pragma define (AstArray< AstExpression* >)
#pragma define (AstArray< AstFormalParameter* >)
#pragma define (AstArray< AstModifier* >)
#pragma define (AstArray< AstEmptyDeclaration* >)
#else
template class AstArray< AstImportDeclaration* >;
template class AstArray< AstVariableDeclarator* >;
template class AstArray< AstMethodDeclaration* >;
template class AstArray< Ast* >;
template class AstArray< AstExpressionStatement* >;
template class AstArray< AstStaticInitializer* >;
template class AstArray< AstBlock* >;
template class AstArray< AstStatement* >;
template class AstArray< AstConstructorDeclaration* >;
template class AstArray< AstBrackets* >;
template class AstArray< AstInterfaceDeclaration* >;
template class AstArray< AstCatchClause* >;
template class AstArray< AstClassDeclaration* >;
template class AstArray< int >;
template class AstArray< AstFieldDeclaration* >;
template class AstArray< AstDimExpr* >;
template class AstArray< CaseElement* >;
template class AstArray< AstExpression* >;
template class AstArray< AstFormalParameter* >;
template class AstArray< AstModifier* >;
template class AstArray< AstEmptyDeclaration* >;
#endif
