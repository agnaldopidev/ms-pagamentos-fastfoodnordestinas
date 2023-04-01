package com.dev.myfood.pagamento.service;

import com.dev.myfood.pagamento.dto.PagamentoDto;
import com.dev.myfood.pagamento.http.PedidoClient;
import com.dev.myfood.pagamento.model.Pagamento;
import com.dev.myfood.pagamento.model.Status;
import com.dev.myfood.pagamento.repository.PagamentoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class PagamentoService {
    @Autowired
    private PagamentoRepository repository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PedidoClient pedido;

    public Page<PagamentoDto> listar(Pageable paginacao) {
        return repository.findAll(paginacao)
                .map(p->modelMapper.map(p, PagamentoDto.class));
    }
    public PagamentoDto buscarPorId(Long id) {
        Pagamento pagamento = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException());

        return modelMapper.map(pagamento, PagamentoDto.class);
    }
    public PagamentoDto cadastrar(PagamentoDto dto) {
        Pagamento pagamento = modelMapper.map(dto, Pagamento.class);
        pagamento.setStatus(Status.CRIADO);
        repository.save(pagamento);

        return modelMapper.map(pagamento, PagamentoDto.class);
    }
    public PagamentoDto atualizar(Long id, PagamentoDto dto) {
        Pagamento pagamento = getOrElseThrow(id);

        pagamento.setValor(dto.getValor());
        pagamento.setNome(dto.getNome());
        pagamento.setNumero(dto.getNumero());
        pagamento.setExpiracao(dto.getExpiracao());
        pagamento.setCodigo(dto.getCodigo());
        pagamento.setStatus(dto.getStatus());
        pagamento.setPedidoId(dto.getPedidoId());
        pagamento.setFormaDePagamentoId(dto.getFormaDePagamentoId());

        repository.save(pagamento);

        return modelMapper.map(pagamento, PagamentoDto.class);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }

    private Pagamento getOrElseThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
    }

    public void confirmarPagamento(Long id){
        Optional<Pagamento> pagamento = repository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO);
        repository.save(pagamento.get());
        pedido.atualizaPagamento(pagamento.get().getPedidoId());
    }

    public void alteraStatus(Long id) {
        Optional<Pagamento> pagamento = repository.findById(id);

        if (!pagamento.isPresent()) {
            throw new EntityNotFoundException();
        }

        pagamento.get().setStatus(Status.CONFIRMADO_SEM_INTEGRACAO);
        repository.save(pagamento.get());

    }
}
