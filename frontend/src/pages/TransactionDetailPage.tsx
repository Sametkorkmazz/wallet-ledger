import { useParams } from "react-router-dom";

export default function TransactionDetailPage() {
  const { id } = useParams();
  return (
    <div className="flex flex-col gap-1">
      <h1 className="text-2xl font-semibold tracking-tight">Transaction</h1>
      <p className="text-sm text-muted-foreground">{id}</p>
    </div>
  );
}
