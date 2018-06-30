#!/bin/bash
readonly ARQUIVO_SAIDA=ResultadosMinerador.log

echo ------------EXECUCAO_INICIADA------------ >> $ARQUIVO_SAIDA
echo ------------LSCPU------------ >> $ARQUIVO_SAIDA
lscpu >> $ARQUIVO_SAIDA
echo ------------LSPCI------------ >> $ARQUIVO_SAIDA
lspci >> $ARQUIVO_SAIDA
echo ------------LSHW----------- >> $ARQUIVO_SAIDA
lshw >> $ARQUIVO_SAIDA
echo ------------XMR-STAK_CHAMADO----------- >> $ARQUIVO_SAIDA
echo XMR-STAK CHAMADO >> $ARQUIVO_SAIDA
date --rfc-3339=ns >> $ARQUIVO_SAIDA
/usr/bin/time -vpao $ARQUIVO_SAIDA ./xmr-stak --currency aeon7 -o pool.aeon.hashvault.pro:3333 -u WmteSzxeCqd2eft5SEqbbQNLxH5feTTZc6KUb3EDv4GCcSrXxh8SxBXPWHipctgrRjWnNMUuFS5BwKLkcTterk7s21qHnEbnf -p work:anders1232@mail.com $@
date --rfc-3339=ns >> $ARQUIVO_SAIDA
echo ------------EXECUCAO_TERMINADA------------ >> $ARQUIVO_SAIDA
