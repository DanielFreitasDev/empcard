(function () {
    'use strict';

    const CHAVE_TEMA = 'empcard-tema';
    const TEMA_CLARO = 'claro';
    const TEMA_ESCURO = 'escuro';

    /**
     * Aplica o tema visual e sincroniza o texto do botao para a proxima acao disponivel.
     * @param {'claro'|'escuro'} temaAtivo tema que sera aplicado na pagina
     * @param {HTMLButtonElement | null} botaoTema botao de alternancia do tema, quando presente
     */
    function aplicarTema(temaAtivo, botaoTema) {
        document.documentElement.setAttribute('data-tema', temaAtivo);

        if (!botaoTema) {
            return;
        }

        const proximoTema = temaAtivo === TEMA_ESCURO ? TEMA_CLARO : TEMA_ESCURO;
        const textoBotao = proximoTema === TEMA_ESCURO ? 'Ativar tema escuro' : 'Ativar tema claro';
        botaoTema.textContent = textoBotao;
        botaoTema.setAttribute('aria-label', textoBotao);
    }

    /**
     * Resolve o tema inicial priorizando:
     * 1) tema ja aplicado no HTML, 2) tema salvo no localStorage, 3) preferencia do sistema.
     * @returns {'claro'|'escuro'} tema inicial
     */
    function obterTemaInicial() {
        const temaNoHtml = document.documentElement.getAttribute('data-tema');
        if (temaNoHtml === TEMA_CLARO || temaNoHtml === TEMA_ESCURO) {
            return temaNoHtml;
        }

        try {
            const temaSalvo = localStorage.getItem(CHAVE_TEMA);
            if (temaSalvo === TEMA_CLARO || temaSalvo === TEMA_ESCURO) {
                return temaSalvo;
            }
        } catch (erro) {
            // Se storage estiver indisponivel, segue com preferencia do sistema.
        }

        return window.matchMedia('(prefers-color-scheme: dark)').matches ? TEMA_ESCURO : TEMA_CLARO;
    }

    /**
     * Configura alternancia de tema para todo o frontend.
     * O template de PDF nao e afetado, pois utiliza CSS proprio e nao carrega este script.
     */
    function configurarTema() {
        const botaoTema = document.querySelector('#alternadorTema');

        function salvarTema(tema) {
            try {
                localStorage.setItem(CHAVE_TEMA, tema);
            } catch (erro) {
                // Nao interrompe a navegacao caso o browser bloqueie persistencia local.
            }
        }

        const temaInicial = obterTemaInicial();
        aplicarTema(temaInicial, botaoTema);

        if (!botaoTema) {
            return;
        }

        botaoTema.addEventListener('click', () => {
            const temaAtual = document.documentElement.getAttribute('data-tema') === TEMA_ESCURO
                ? TEMA_ESCURO
                : TEMA_CLARO;
            const proximoTema = temaAtual === TEMA_ESCURO ? TEMA_CLARO : TEMA_ESCURO;
            aplicarTema(proximoTema, botaoTema);
            salvarTema(proximoTema);
        });
    }

    /**
     * Remove tudo que nao for digito para simplificar mascaras e validacoes.
     * @param {string} valor texto de entrada
     * @returns {string} apenas numeros
     */
    function somenteDigitos(valor) {
        return (valor || '').replace(/\D/g, '');
    }

    /**
     * Aplica mascara de CPF no formato XXX.XXX.XXX-XX.
     * @param {string} valor texto digitado
     * @returns {string} texto mascarado
     */
    function mascararCpf(valor) {
        const digitos = somenteDigitos(valor).slice(0, 11);
        return digitos
            .replace(/(\d{3})(\d)/, '$1.$2')
            .replace(/(\d{3})(\d)/, '$1.$2')
            .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
    }

    /**
     * Aplica mascara de cartao com blocos de 4 digitos.
     * @param {string} valor texto digitado
     * @returns {string} numero mascarado
     */
    function mascararCartao(valor) {
        const digitos = somenteDigitos(valor).slice(0, 16);
        return digitos.replace(/(\d{4})(?=\d)/g, '$1 ').trim();
    }

    /**
     * Aplica mascara basica de telefone para celular/WhatsApp.
     * @param {string} valor texto digitado
     * @returns {string} telefone mascarado
     */
    function mascararTelefone(valor) {
        const digitos = somenteDigitos(valor).slice(0, 11);
        if (digitos.length <= 10) {
            return digitos
                .replace(/(\d{2})(\d)/, '($1) $2')
                .replace(/(\d{4})(\d{1,4})$/, '$1-$2');
        }
        return digitos
            .replace(/(\d{2})(\d)/, '($1) $2')
            .replace(/(\d{5})(\d{1,4})$/, '$1-$2');
    }

    /**
     * Aplica mascara de CEP no formato 00000-000.
     * @param {string} valor texto digitado
     * @returns {string} cep mascarado
     */
    function mascararCep(valor) {
        const digitos = somenteDigitos(valor).slice(0, 8);
        return digitos.replace(/(\d{5})(\d)/, '$1-$2');
    }

    /**
     * Formata para moeda BRL com prefixo R$ durante a digitacao.
     * @param {string} valor texto digitado
     * @returns {string} valor formatado em reais
     */
    function mascararMoeda(valor) {
        const digitos = somenteDigitos(valor);
        if (!digitos) {
            return '';
        }
        const numero = Number(digitos) / 100;
        return new Intl.NumberFormat('pt-BR', {
            style: 'currency',
            currency: 'BRL'
        }).format(numero);
    }

    /**
     * Configura eventos de mascara para todos os campos declarados.
     */
    function configurarMascaras() {
        document.querySelectorAll('.mask-cpf').forEach((input) => {
            input.addEventListener('input', () => {
                input.value = mascararCpf(input.value);
            });
            input.value = mascararCpf(input.value);
        });

        document.querySelectorAll('.mask-cartao').forEach((input) => {
            input.addEventListener('input', () => {
                input.value = mascararCartao(input.value);
            });
            input.value = mascararCartao(input.value);
        });

        document.querySelectorAll('.mask-cep').forEach((input) => {
            input.addEventListener('input', () => {
                input.value = mascararCep(input.value);
            });
            input.value = mascararCep(input.value);
        });

        document.querySelectorAll('.mask-celular').forEach((input) => {
            input.addEventListener('input', () => {
                input.value = mascararTelefone(input.value);
            });
            input.value = mascararTelefone(input.value);
        });

        document.querySelectorAll('.mask-money').forEach((input) => {
            input.addEventListener('input', () => {
                input.value = mascararMoeda(input.value);
            });
            input.value = mascararMoeda(input.value);
        });
    }

    /**
     * Forca uppercase em campos textuais para aderir regra de negocio.
     */
    function configurarMaiusculas() {
        const tiposIgnorados = ['email', 'number', 'date', 'password', 'checkbox', 'hidden', 'radio', 'month'];

        document.querySelectorAll('input, textarea').forEach((campo) => {
            const tipo = (campo.getAttribute('type') || 'text').toLowerCase();
            if (tiposIgnorados.includes(tipo) || campo.dataset.noUppercase === 'true') {
                return;
            }
            campo.addEventListener('input', () => {
                campo.value = campo.value.toUpperCase();
            });
            if (campo.value) {
                campo.value = campo.value.toUpperCase();
            }
        });
    }

    /**
     * Antes de submeter formularios, remove mascara dos campos numericos para persistencia limpa.
     */
    function configurarSanitizacaoNoSubmit() {
        document.querySelectorAll('form').forEach((formulario) => {
            formulario.addEventListener('submit', () => {
                formulario.querySelectorAll('[data-digits-only="true"]').forEach((input) => {
                    input.value = somenteDigitos(input.value);
                });
            });
        });
    }

    /**
     * Integra preenchimento automatico do endereco via API ViaCEP.
     */
    function configurarViaCep() {
        const cepInput = document.querySelector('#cepPessoa');
        if (!cepInput) {
            return;
        }

        const campos = {
            logradouro: document.querySelector('#logradouroPessoa'),
            complemento: document.querySelector('#complementoPessoa'),
            bairro: document.querySelector('#bairroPessoa'),
            cidade: document.querySelector('#cidadePessoa'),
            estado: document.querySelector('#estadoPessoa')
        };

        function aplicarCampo(campo, valor) {
            if (!campo) {
                return;
            }

            const valorTexto = (valor || '').toString().trim().toUpperCase();
            if (valorTexto) {
                campo.value = valorTexto;
                campo.readOnly = true;
            } else {
                campo.readOnly = false;
            }
        }

        async function buscarCep() {
            const cep = somenteDigitos(cepInput.value);
            if (cep.length !== 8) {
                return;
            }

            try {
                const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
                if (!response.ok) {
                    return;
                }

                const dados = await response.json();
                if (dados.erro) {
                    return;
                }

                aplicarCampo(campos.logradouro, dados.logradouro);
                aplicarCampo(campos.complemento, dados.complemento);
                aplicarCampo(campos.bairro, dados.bairro);
                aplicarCampo(campos.cidade, dados.localidade);
                aplicarCampo(campos.estado, dados.uf);
            } catch (error) {
                // Falhas de rede nao devem impedir preenchimento manual do cadastro.
                console.error('Falha ao consultar ViaCEP:', error);
            }
        }

        cepInput.addEventListener('blur', buscarCep);
    }

    /**
     * Replica numero do celular em WhatsApp quando checkbox estiver marcada.
     */
    function configurarWhatsappIgualCelular() {
        const checkbox = document.querySelector('#whatsappMesmoCelular');
        const campoCelular = document.querySelector('#celularPessoa');
        const campoWhatsapp = document.querySelector('#whatsappPessoa');
        if (!checkbox || !campoCelular || !campoWhatsapp) {
            return;
        }

        function sincronizar() {
            if (checkbox.checked) {
                campoWhatsapp.value = campoCelular.value;
                campoWhatsapp.readOnly = true;
            } else {
                campoWhatsapp.readOnly = false;
            }
        }

        checkbox.addEventListener('change', sincronizar);
        campoCelular.addEventListener('input', () => {
            if (checkbox.checked) {
                campoWhatsapp.value = campoCelular.value;
            }
        });

        sincronizar();
    }

    /**
     * Ajusta comportamento do formulario de lancamentos conforme tipo selecionado.
     */
    function configurarFormularioLancamento() {
        const tipo = document.querySelector('#tipoLancamento');
        const parcelas = document.querySelector('#quantidadeParcelas');
        const grupoDataFim = document.querySelector('#grupoDataFimFixo');
        if (!tipo || !parcelas || !grupoDataFim) {
            return;
        }

        function atualizar() {
            if (tipo.value === 'PARCELADO') {
                parcelas.disabled = false;
                parcelas.readOnly = false;
                parcelas.min = '2';
                if (Number(parcelas.value) < 2) {
                    parcelas.value = '2';
                }
                grupoDataFim.style.display = 'none';
            } else if (tipo.value === 'FIXO') {
                // Campo desabilitado nao e enviado no submit; por isso usamos readOnly.
                parcelas.disabled = false;
                parcelas.readOnly = true;
                parcelas.min = '1';
                parcelas.value = '1';
                grupoDataFim.style.display = 'block';
            } else {
                parcelas.disabled = false;
                parcelas.readOnly = true;
                parcelas.min = '1';
                parcelas.value = '1';
                grupoDataFim.style.display = 'none';
            }
        }

        tipo.addEventListener('change', atualizar);
        atualizar();
    }

    /**
     * Exibe confirmacao padrao para acoes criticas (desativar/excluir).
     */
    function configurarConfirmacoes() {
        document.querySelectorAll('[data-confirm]').forEach((botao) => {
            botao.addEventListener('click', (evento) => {
                const mensagem = botao.getAttribute('data-confirm');
                if (mensagem && !window.confirm(mensagem)) {
                    evento.preventDefault();
                }
            });
        });
    }

    configurarTema();
    configurarMascaras();
    configurarMaiusculas();
    configurarViaCep();
    configurarWhatsappIgualCelular();
    configurarFormularioLancamento();
    configurarConfirmacoes();
    configurarSanitizacaoNoSubmit();
})();
