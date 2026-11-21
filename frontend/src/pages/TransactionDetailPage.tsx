import { useParams } from "react-router-dom";

export default function TransactionDetailPage() {
  const { id } = useParams();
  return (
    <div className="flex flex-col gap-1">
      <h1 className="page-title">Transaction</h1>
      <p className="caption">{id}</p>
    </div>
  );
}
